package org.amshove.natls.project;

import org.amshove.natlint.linter.NaturalLinter;
import org.amshove.natls.DiagnosticOriginalUri;
import org.amshove.natls.DiagnosticTool;
import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natls.progress.IProgressMonitor;
import org.amshove.natls.progress.NullProgressMonitor;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.lexing.TokenList;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.ddm.IDataDefinitionModule;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.amshove.natparse.natural.project.NaturalProgrammingMode;
import org.amshove.natparse.parsing.DefineDataParser;
import org.amshove.natparse.parsing.IModuleProvider;
import org.amshove.natparse.parsing.NaturalModule;
import org.amshove.natparse.parsing.NaturalParser;
import org.amshove.natparse.parsing.ddm.DdmParser;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Stream;

public class LanguageServerFile implements IModuleProvider
{
	private static final Logger log = LoggerFactory.getLogger(LanguageServerFile.class);
	private final NaturalFile file;
	private final Map<String, List<Diagnostic>> diagnosticsByTool = new HashMap<>();
	private INaturalModule module;
	private LanguageServerLibrary library;
	private final Set<LanguageServerFile> outgoingReferences = new HashSet<>();
	private final Set<LanguageServerFile> incomingReferences = new HashSet<>();
	private TokenList tokens;

	private byte[] defineDataHash;
	private boolean hasBeenAnalyzed;

	public LanguageServerFile(NaturalFile file)
	{
		this.file = file;
	}

	public static LanguageServerFile fromFile(NaturalFile file)
	{
		return new LanguageServerFile(file);
	}

	public List<Diagnostic> allDiagnostics()
	{
		return diagnosticsByTool.values().stream().flatMap(Collection::stream).toList();
	}

	public Set<LanguageServerFile> getOutgoingReferences()
	{
		return outgoingReferences;
	}

	public Set<LanguageServerFile> getIncomingReferences()
	{
		return incomingReferences;
	}

	public void addDiagnostic(DiagnosticTool tool, Diagnostic diagnostic)
	{
		getDiagnosticsByTool(tool.getId()).add(diagnostic);
	}

	public void addDiagnostic(DiagnosticTool tool, IDiagnostic diagnostic)
	{
		getDiagnosticsByTool(tool.getId()).add(LspUtil.toLspDiagnostic(tool.getId(), diagnostic));
	}

	public void clearDiagnosticsByTool(DiagnosticTool tool)
	{
		getDiagnosticsByTool(tool.getId()).clear();
	}

	private List<Diagnostic> getDiagnosticsByTool(String tool)
	{
		return diagnosticsByTool.computeIfAbsent(tool, (k) -> new ArrayList<>());
	}

	public String getUri()
	{
		return file.getPath().toUri().toString();
	}

	public Path getPath()
	{
		return file.getPath();
	}

	public void open()
	{
		// Always reparse on open. In the past, some files weren't analyzed correctly because
		// they've been parsed on another path. This resulted in diagnostics not showing up.
		parse(ParseStrategy.WITHOUT_CALLERS);

		if (!hasBeenAnalyzed || allDiagnostics().isEmpty())
		{
			analyze();
		}
	}

	public void close()
	{
		//		module = null;
		//		clearDiagnosticsByTool(DiagnosticTool.NATPARSE);
	}

	public void changed(String newSource)
	{
		clearDiagnosticsByTool(DiagnosticTool.CATALOG);
		parseAndAnalyze(newSource, ParseStrategy.WITH_CALLERS);
	}

	public void save()
	{
		clearDiagnosticsByTool(DiagnosticTool.CATALOG);
		clearDiagnosticsByTool(DiagnosticTool.NATLINT);
		clearDiagnosticsByTool(DiagnosticTool.NATPARSE);
		parse();
	}

	public void parse(ParseStrategy strategy)
	{
		try
		{
			parseAndAnalyze(Files.readString(file.getPath()), strategy);
		}
		catch (Exception e)
		{
			log.error("Error during parse from <%s>".formatted(file.getPath()), e);
			addDiagnostic(
				DiagnosticTool.NATPARSE,
				new Diagnostic(
					new Range(
						new Position(0, 0),
						new Position(0, 0)
					),
					"Unhandled exception: %s".formatted(e.getMessage())
				)
			);
		}
	}

	public void parse()
	{
		parse(ParseStrategy.WITH_CALLERS);
	}

	private boolean hasToReparseCallers(String newSource)
	{
		var newDefineDataHash = hashDefineData(newSource);
		var defineDataChanged = !Arrays.equals(newDefineDataHash, defineDataHash);
		defineDataHash = newDefineDataHash;
		var tooManyCallers = incomingReferences.size() > 20;
		// TODO: Add trace log?

		return !tooManyCallers && defineDataChanged;
	}

	private void parseAndAnalyze(String source, ParseStrategy strategy)
	{
		try
		{
			var previousCallers = module != null ? module.callers() : ReadOnlyList.<IModuleReferencingNode> from(List.of());
			reparseWithoutAnalyzing(source);

			analyze();
			hasBeenAnalyzed = true;

			if (strategy != ParseStrategy.WITHOUT_CALLERS && hasToReparseCallers(source))
			{
				reparseCallers();
			}
			else
			{
				for (var previousCaller : previousCallers)
				{
					module.addCaller(previousCaller);
				}
			}
		}
		catch (Exception e)
		{
			log.error("Error during parseAndAnalyze <%s>".formatted(file.getPath()), e);
			addDiagnostic(
				DiagnosticTool.NATPARSE,
				new Diagnostic(
					new Range(
						new Position(0, 0),
						new Position(0, 0)
					),
					"Unhandled exception: %s".formatted(e.getMessage())
				)
			);
		}
	}

	private void analyze()
	{
		clearDiagnosticsByTool(DiagnosticTool.NATLINT);
		if (module.programmingMode() == NaturalProgrammingMode.REPORTING)
		{
			// Reporting mode not supported by natparse
			return;
		}

		var linter = new NaturalLinter();
		var linterDiagnostics = linter.lint(module);
		for (var linterDiagnostic : linterDiagnostics)
		{
			addDiagnostic(DiagnosticTool.NATLINT, linterDiagnostic);
		}
	}

	public void reparseCallers()
	{
		reparseCallers(new NullProgressMonitor());
	}

	public void reparseCallers(IProgressMonitor monitor)
	{
		monitor.progress("Parsing callers", 0);
		var callers = new ArrayList<>(incomingReferences);
		incomingReferences.clear();
		// TODO: Add LSP Progress
		for (var languageServerFile : callers)
		{
			if (monitor.isCancellationRequested())
			{
				break;
			}

			if (languageServerFile == this)
			{
				// recursive calls, we don't need to parse ourselves again
				continue;
			}

			monitor.progress("Parsing caller %s".formatted(languageServerFile.getReferableName()), 5);

			try
			{
				languageServerFile.reparseWithoutAnalyzing();
			}
			catch (Exception e)
			{
				log.error("Error during reparseCallers from <%s> for <%s>".formatted(file.getPath(), languageServerFile.getPath()), e);
				addDiagnostic(
					DiagnosticTool.NATPARSE,
					new Diagnostic(
						new Range(
							new Position(0, 0),
							new Position(0, 0)
						),
						"Unhandled exception: %s".formatted(e.getMessage())
					)
				);
			}
		}
	}

	private void reparseWithoutAnalyzing() throws IOException
	{
		reparseWithoutAnalyzing(Files.readString(file.getPath()));
	}

	private void reparseWithoutAnalyzing(String source)
	{
		hasBeenAnalyzed = false;
		if (module != null)
		{
			destroyPresentNodes();
		}

		// Evict ourselves from cached module references, as we're about to parse outgoing
		// references by parsed Nodes.
		// Perf: Only do so if we have outgoing references, as evicting is expensive.
		if (!outgoingReferences.isEmpty())
		{
			ModuleReferenceCache.evictMyReferences(this);
		}

		outgoingReferences.forEach(ref -> ref.removeIncomingReference(this));
		outgoingReferences.clear(); // Will be re-added during parse
		clearDiagnosticsByTool(DiagnosticTool.NATPARSE);

		var lexer = new Lexer();
		tokens = lexer.lex(source, file.getPath());
		var parser = new NaturalParser(this);

		module = parser.parse(file, tokens);
		for (var diagnostic : module.diagnostics())
		{
			addDiagnostic(DiagnosticTool.NATPARSE, diagnostic);
		}
	}

	private void destroyPresentNodes()
	{
		if (module instanceof IHasDefineData hasDefineData && hasDefineData.defineData() != null)
		{
			hasDefineData.defineData().descendants().forEach(ISyntaxNode::destroy);
		}

		if (module instanceof IModuleWithBody hasBody && hasBody.body() != null)
		{
			hasBody.body().destroy();
		}
	}

	public INaturalModule module(ParseStrategy strategy)
	{
		if (module == null || module.syntaxTree() == null) // TODO: Use parsed flag to determine if its only partial parsed. SyntaxTree is conveniently null currently, but that's not reliable
		{
			parse(strategy);
		}
		return module;
	}

	public INaturalModule module()
	{
		return module(ParseStrategy.WITH_CALLERS);
	}

	// TODO(cyclic-dependencies):
	//   Currently necessary for dependency loops which would cause a stack overflow. e.g. MOD1 -> MOD2 -> MOD1 ...
	//   Solution might be to instantiate modules while indexing, only replacing stuff with the parser
	private INaturalModule parseDefineDataOnly()
	{
		if (module != null)
		{
			return module;
		}

		try
		{
			var source = Files.readString(file.getPath());
			var lexer = new Lexer();
			tokens = lexer.lex(source, file.getPath());
			var defineDataParser = new DefineDataParser(this);
			var definedata = defineDataParser.parse(tokens);
			var module = new NaturalModule(file);
			module.setDefineData(definedata.result());
			module.setComments(tokens.comments());
			this.module = module;
		}
		catch (Exception e)
		{
			log.error("Error during parseDefineDataOnly from <%s>".formatted(file.getPath()), e);
			addDiagnostic(
				DiagnosticTool.NATPARSE,
				new Diagnostic(
					new Range(
						new Position(0, 0),
						new Position(0, 0)
					),
					"Unhandled exception: %s".formatted(e.getMessage())
				)
			);
		}

		return module;
	}

	void setLibrary(LanguageServerLibrary library)
	{
		this.library = library;
	}

	public LanguageServerLibrary getLibrary()
	{
		return library;
	}

	public NaturalFileType getType()
	{
		return file.getFiletype();
	}

	public String getReferableName()
	{
		return file.getReferableName();
	}

	@Override
	public INaturalModule findNaturalModule(String referableName)
	{
		var calledFile = library.provideNaturalModule(referableName, true);
		if (calledFile == null)
		{
			return null;
		}

		addOutgoingReference(calledFile);
		calledFile.addIncomingReference(this);
		return calledFile.parseDefineDataOnly();
	}

	@Override
	public IDataDefinitionModule findDdm(String referableName)
	{
		var calledFile = library.provideDdm(referableName, true);
		if (calledFile == null)
		{
			return null;
		}

		try
		{
			return new DdmParser().parseDdm(Files.readString(calledFile.getPath()));
		}
		catch (IOException e)
		{
			log.error("Error during findDdm from <%s>".formatted(file.getPath()), e);
			throw new UncheckedIOException(e);
		}
	}

	void addIncomingReference(LanguageServerFile caller)
	{
		incomingReferences.add(caller);
	}

	private void removeIncomingReference(LanguageServerFile caller)
	{
		if (module != null)
		{
			for (var callerNode : module.callers())
			{
				if (callerNode.referencingToken().filePath().equals(caller.file.getPath()))
				{
					module.removeCaller(callerNode);
				}
			}
		}

		incomingReferences.remove(caller);
	}

	void addOutgoingReference(LanguageServerFile calledModule)
	{
		outgoingReferences.add(calledModule);
	}

	private void removeOutgoingReference(LanguageServerFile calledModule)
	{
		outgoingReferences.remove(calledModule);
	}

	public void clearAllIncomingAndOutgoingReferences()
	{
		outgoingReferences.forEach(ref -> ref.removeIncomingReference(this));
		outgoingReferences.clear();
		incomingReferences.forEach(ref -> ref.removeOutgoingReference(this));
		incomingReferences.clear();
	}

	public List<Diagnostic> diagnosticsInRange(Range range)
	{
		return allDiagnostics().stream().filter(d -> LspUtil.isInSameLine(d.getRange(), range)).toList();
	}

	public NaturalFile getNaturalFile()
	{
		return file;
	}

	private byte[] hashDefineData(String source)
	{
		try
		{
			var md5 = MessageDigest.getInstance("MD5");
			var defineDataStartIndex = source.indexOf("DEFINE DATA");
			var defineDataEndIndex = source.indexOf("END-DEFINE");
			var defineData = source.substring(defineDataStartIndex, defineDataEndIndex);
			return md5.digest(defineData.getBytes(StandardCharsets.UTF_8));
		}
		catch (Exception e)
		{
			return new byte[0];
		}
	}

	/**
	 * Streams all tokens from the current parsed module.
	 */
	public Stream<SyntaxToken> tokens()
	{
		if (tokens == null)
		{
			parse(ParseStrategy.WITHOUT_CALLERS);
		}

		return tokens.stream();
	}

	public ReadOnlyList<Diagnostic> diagnosticsInFileOfType(String id)
	{
		return ReadOnlyList.from(allDiagnostics().stream().filter(d -> d.getCode().getLeft().equals(id)).filter(this::containsDiagnostic).toList());
	}

	public boolean containsDiagnostic(Diagnostic diagnostic)
	{
		if (diagnostic.getData()instanceof DiagnosticOriginalUri originalUri)
		{
			return originalUri.getUri().equals(getUri());
		}

		// Diagnostics raised by the language server, not parser or linter
		return false;
	}
}
