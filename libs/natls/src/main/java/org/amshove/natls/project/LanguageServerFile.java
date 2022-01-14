package org.amshove.natls.project;

import org.amshove.natls.DiagnosticTool;
import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.amshove.natparse.parsing.IModuleProvider;
import org.amshove.natparse.parsing.NaturalParser;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class LanguageServerFile implements IModuleProvider
{
	private final NaturalFile file;
	private final Map<String, List<Diagnostic>> diagnosticsByTool = new HashMap<>();
	private INaturalModule module;
	private LanguageServerLibrary library;
	private final List<LanguageServerFile> outgoingReferences = new ArrayList<>();
	private final List<LanguageServerFile> incomingReferences = new ArrayList<>();

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

	public List<LanguageServerFile> getOutgoingReferences()
	{
		return outgoingReferences;
	}

	public List<LanguageServerFile> getIncomingReferences()
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
		if(module == null)
		{
			parse(false);
		}
	}

	public void close()
	{
//		module = null;
//		clearDiagnosticsByTool(DiagnosticTool.NATPARSE);
	}

	void dependencyChanged()
	{
		parse(true);
	}

	public void changed(String newSource)
	{
		// Tuning: Reduce the load if the module has too many dependants.
		// 	Its fair enough to reparse the dependants on save only when changing
		//  a central module
		var reparseDependants = incomingReferences.size() < 20;
		parseInternal(newSource, reparseDependants);
	}

	public void save()
	{
		parse(true);
	}

	public void parse(boolean reparseCallers)
	{
		try
		{
			parseInternal(Files.readString(file.getPath()), reparseCallers);
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

	private void parseInternal(String source, boolean reparseCallers)
	{
		try
		{
			System.err.printf("Parsing %s%n", file.getReferableName());
			System.err.printf("I have %d dependants%n", incomingReferences.size());
			outgoingReferences.forEach(ref -> ref.removeIncomingReference(this));
			outgoingReferences.clear(); // Will be added when we let our callers parse again
			clearDiagnosticsByTool(DiagnosticTool.NATPARSE);

			var lexer = new Lexer();
			var tokenList = lexer.lex(source, file.getPath());
			var parser = new NaturalParser(this);

			module = parser.parse(file, tokenList);
			for (var diagnostic : module.diagnostics())
			{
				addDiagnostic(DiagnosticTool.NATPARSE, diagnostic);
			}

			// lint
			// clearByTool NATLINT
			// add linter diagnostics

			if (reparseCallers)
			{
				var callers = new ArrayList<>(incomingReferences);
				incomingReferences.clear();
				// TODO: Add LSP Progress
				callers.forEach(LanguageServerFile::dependencyChanged);
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

	public INaturalModule module()
	{
		if(module == null)
		{
			parse(false);
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
		if(calledFile == null) {
			return null;
		}

		addOutgoingReference(calledFile);
		calledFile.addIncomingReference(this);
		return calledFile.module();
	}

    void addIncomingReference(LanguageServerFile caller)
    {
        incomingReferences.add(caller);
    }

	private void removeIncomingReference(LanguageServerFile caller)
	{
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
}
