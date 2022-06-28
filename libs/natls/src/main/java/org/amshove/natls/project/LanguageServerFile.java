package org.amshove.natls.project;

import org.amshove.natlint.linter.NaturalLinter;
import org.amshove.natls.DiagnosticTool;
import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.ddm.IDataDefinitionModule;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.amshove.natparse.parsing.DefineDataParser;
import org.amshove.natparse.parsing.IModuleProvider;
import org.amshove.natparse.parsing.NaturalModule;
import org.amshove.natparse.parsing.NaturalParser;
import org.amshove.natparse.parsing.ddm.DdmParser;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;

public class LanguageServerFile implements IModuleProvider
{
	private final NaturalFile file;
	private final Map<String, List<Diagnostic>> diagnosticsByTool = new HashMap<>();
	private INaturalModule module;
	private LanguageServerLibrary library;
	private final Set<LanguageServerFile> outgoingReferences = new HashSet<>();
	private final Set<LanguageServerFile> incomingReferences = new HashSet<>();
	private final List<SyntaxToken> comments = new ArrayList<>();

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
		if (module == null)
		{
			parse();
		}

		if(!hasBeenAnalyzed)
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
		parseAndAnalyze(newSource);
	}

	public void save()
	{
		clearDiagnosticsByTool(DiagnosticTool.CATALOG);
		clearDiagnosticsByTool(DiagnosticTool.NATLINT);
		clearDiagnosticsByTool(DiagnosticTool.NATPARSE);
		parse();
	}

	public void parse()
	{
		try
		{
			parseAndAnalyze(Files.readString(file.getPath()));
		}
		catch (Exception e)
		{
			addDiagnostic(DiagnosticTool.NATPARSE,
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

	private boolean hasToReparseCallers(String newSource)
	{
		var newDefineDataHash = hashDefineData(newSource);
		var defineDataChanged = !Arrays.equals(newDefineDataHash, defineDataHash);
		defineDataHash = newDefineDataHash;
		var tooManyCallers = incomingReferences.size() > 20;
		// TODO: Add trace log?

		return !tooManyCallers && defineDataChanged;
	}

	private void parseAndAnalyze(String source)
	{
		try
		{
			var previousCallers = module != null ? module.callers() : ReadOnlyList.<IModuleReferencingNode>from(List.of());
			reparseWithoutAnalyzing(source);

			analyze();
			hasBeenAnalyzed = true;

			if (hasToReparseCallers(source))
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
			addDiagnostic(DiagnosticTool.NATPARSE,
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
		var start = System.currentTimeMillis();
		var log = "Analyzing %s".formatted(getReferableName());
		clearDiagnosticsByTool(DiagnosticTool.NATLINT);
		var linter = new NaturalLinter();
		var linterDiagnostics = linter.lint(module);
		for (var linterDiagnostic : linterDiagnostics)
		{
			addDiagnostic(DiagnosticTool.NATLINT, linterDiagnostic);
		}
		var end = System.currentTimeMillis();
		log += " took %dms".formatted(end - start);
		System.err.println(log);
	}

	public void reparseCallers()
	{
		var callers = new ArrayList<>(incomingReferences);
		incomingReferences.clear();
		// TODO: Add LSP Progress
		callers.forEach(languageServerFile -> {
			if (languageServerFile == this)
			{
				// recursive calls, we don't need to parse ourselves again
				return;
			}
			try
			{
				languageServerFile.reparseWithoutAnalyzing();
			}
			catch (Exception e)
			{
				addDiagnostic(DiagnosticTool.NATPARSE,
					new Diagnostic(
						new Range(
							new Position(0, 0),
							new Position(0, 0)
						),
						"Unhandled exception: %s".formatted(e.getMessage())
					)
				);
			}
		});
	}

	private void reparseWithoutAnalyzing() throws IOException
	{
		reparseWithoutAnalyzing(Files.readString(file.getPath()));
	}

	private void reparseWithoutAnalyzing(String source)
	{
		hasBeenAnalyzed = false;
		var start = System.currentTimeMillis();
		var log = "Parsing %s".formatted(getReferableName());
		if (module != null)
		{
			destroyPresentNodes();
			log += " (destroyed previous nodes)";
		}
		System.err.println(log);

		outgoingReferences.forEach(ref -> ref.removeIncomingReference(this));
		outgoingReferences.clear(); // Will be re-added during parse
		clearDiagnosticsByTool(DiagnosticTool.NATPARSE);

		var lexer = new Lexer();
		var tokenList = lexer.lex(source, file.getPath());
		comments.clear();
		comments.addAll(tokenList.comments().toList());
		var parser = new NaturalParser(this);

		module = parser.parse(file, tokenList);
		for (var diagnostic : module.diagnostics())
		{
			addDiagnostic(DiagnosticTool.NATPARSE, diagnostic);
		}
		var end = System.currentTimeMillis();
		System.err.printf("Took %dms%n", end - start);
	}

	private void destroyPresentNodes()
	{
		if (module instanceof IHasDefineData hasDefineData && hasDefineData.defineData() != null)
		{
			hasDefineData.defineData().descendants().forEach(ISyntaxNode::destroy);
		}

		if (module instanceof IHasBody hasBody && hasBody.body() != null)
		{
			hasBody.body().destroy();
		}
	}

	public INaturalModule module()
	{
		if (module == null || module.syntaxTree() == null) // TODO: Use parsed flag to determine if its only partial parsed. SyntaxTree is conveniently null currently, but that's not reliable
		{
			parse();
		}
		return module;
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
			var tokenList = lexer.lex(source, file.getPath());
			var defineDataParser = new DefineDataParser(this);
			var definedata = defineDataParser.parse(tokenList);
			var module = new NaturalModule(file);
			module.setDefineData(definedata.result());
			this.module = module;
		}
		catch (Exception e)
		{
			addDiagnostic(DiagnosticTool.NATPARSE,
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
		var calledFile = library.provideNaturalFile(referableName, true);
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
		var calledFile = library.provideNaturalFile(referableName, true);
		if(calledFile == null)
		{
			return null;
		}

		try
		{
			return new DdmParser().parseDdm(Files.readString(calledFile.getPath()));
		}
		catch (IOException e)
		{
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

	public ReadOnlyList<SyntaxToken> comments()
	{
		return ReadOnlyList.from(comments);
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
}
