package org.amshove.natls.project;

import org.amshove.natls.DiagnosticTool;
import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.amshove.natparse.parsing.NaturalParser;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class LanguageServerFile
{
	private final NaturalFile file;
	private final Map<String, List<Diagnostic>> diagnosticsByTool = new HashMap<>();
	private INaturalModule module;
	private LanguageServerLibrary library;

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
		parse();
	}

	public void close()
	{
		module = null;
		clearDiagnosticsByTool(DiagnosticTool.NATPARSE);
	}

	public void changed(String newSource)
	{
		parseInternal(newSource);
	}

	public void save()
	{
		parse();
	}

	public void parse()
	{
		try
		{
			parseInternal(Files.readString(file.getPath()));
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

	private void parseInternal(String source)
	{
		try
		{
			clearDiagnosticsByTool(DiagnosticTool.NATPARSE);

			var lexer = new Lexer();
			var tokenList = lexer.lex(source);
			var parser = new NaturalParser();

			module = parser.parse(file, tokenList);
			for (var diagnostic : module.diagnostics())
			{
				addDiagnostic(DiagnosticTool.NATPARSE, diagnostic);
			}

			// lint
			// clearByTool NATLINT
			// add linter diagnostics
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
			parse();
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
}
