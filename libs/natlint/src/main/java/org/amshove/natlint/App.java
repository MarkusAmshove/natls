package org.amshove.natlint;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.IPosition;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.infrastructure.ActualFilesystem;
import org.amshove.natparse.infrastructure.IFilesystem;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.natural.project.NaturalProjectFileIndexer;
import org.amshove.natparse.parsing.NaturalParser;
import org.amshove.natparse.parsing.project.BuildFileProjectReader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;

public class App
{
	private final Path projectFile;
	private final IFilesystem filesystem;

	public App(Path projectFile, IFilesystem filesystem)
	{
		this.projectFile = projectFile;
		this.filesystem = filesystem;
	}

	public static void main(String[] args)
	{
		var workingDirectoryPath = System.getProperty("user.dir");
		var workingDirectory = Paths.get(workingDirectoryPath);
		var filesystem = new ActualFilesystem();

		while (!workingDirectory.getRoot().equals(workingDirectory) && filesystem.findFile("_naturalBuild", workingDirectory).isEmpty())
		{
			workingDirectory = workingDirectory.getParent();
		}

		var projectFile = filesystem.findFile("_naturalBuild", workingDirectory);

		if (workingDirectory.getRoot().equals(workingDirectory) || projectFile.isEmpty())
		{
			throw new RuntimeException("Project root could not be determined. _naturalBuild file not found");
		}

		System.out.printf("""
			     .@@@@@@@@@@@@@@@&
			    /@@@@@@@@@@@@@@@@@.          %s
			     @@@@@@@@*@@@@@@@@           Version: %s
			  ....###############
			......###.@/##.@.####......
			     .###############
			     .###############
			     .###############
			       ############.
			           ....
			%n""", App.class.getPackage().getImplementationTitle(), App.class.getPackage().getImplementationVersion());
		new App(projectFile.get(), filesystem).run();
	}

	private static final Comparator<IDiagnostic> byLineNumber = Comparator.comparingInt(IPosition::line);

	private static record DiagnosticByCount(String message, int count){}

	public void run()
	{
		var startIndex = System.currentTimeMillis();
		var project = new BuildFileProjectReader(filesystem).getNaturalProject(projectFile);
		var indexer = new NaturalProjectFileIndexer(filesystem);
		indexer.indexProject(project);
		var endIndex = System.currentTimeMillis();

		var lexer = new Lexer();
		var parser = new NaturalParser();
		var diagnosticsPerType = new HashMap<String, Integer>();
		var filesChecked = 0;
		var totalDiagnostics = 0;
		var startCheck = System.currentTimeMillis();
		for (var library : project.getLibraries())
		{
			for (var file : library.files().stream().filter(f -> f.getFiletype().hasDefineData()).toList())
			{
				filesChecked++;
				var filePath = file.getPath();
				try
				{
					var tokens = lexer.lex(filesystem.readFile(filePath), filePath);
					var module = parser.parse(file, tokens);

					module.diagnostics().forEach(d -> {
						var count = diagnosticsPerType.computeIfAbsent(d.message(), (k) -> 0);
						count++;
						diagnosticsPerType.replace(d.message(), count);
					});
					totalDiagnostics += module.diagnostics().size();
					printDiagnostics(filePath, module.diagnostics());
				}
				catch(Exception e)
				{
					System.err.println(filePath);
					throw e;
				}
			}
		}
		var endCheck = System.currentTimeMillis();

		var indexTime = endIndex - startIndex;
		var checkTime = endCheck - startCheck;
		System.out.println();
		System.out.println("Done.");
		System.out.println("Index time: " + indexTime + " ms");
		System.out.println("Check time: " + checkTime + " ms");
		System.out.println("Total: " + (indexTime + checkTime) + " ms");
		System.out.println("Files checked: " + filesChecked);
		System.out.println("Total diagnostics: " + totalDiagnostics);
		System.out.println();
		diagnosticsPerType.entrySet().stream().map((entry) ->
			new DiagnosticByCount(entry.getKey(), entry.getValue())).sorted(Comparator.comparingInt(DiagnosticByCount::count)).toList().forEach(d -> {
			System.out.println(d.message + "|" + d.count);
		});
	}

	private void printDiagnostics(Path filePath, ReadOnlyList<IDiagnostic> diagnostics)
	{
		if (diagnostics.isEmpty())
		{
			return;
		}

		var sortedDiagnostics = diagnostics.stream().sorted(byLineNumber).toList();

		for (var diagnostic : sortedDiagnostics)
		{
			System.out.printf("%s:%d:%d", filePath, diagnostic.line() + 1, diagnostic.offsetInLine());

			System.out.println();
			System.out.println(readDiagnosticSourceLine(filePath, diagnostic));
			System.out.println(squiggle(diagnostic));
			System.out.println(message(diagnostic));
			System.out.println();
		}

		System.out.println();
	}

	private String message(IDiagnostic diagnostic)
	{
		var message = new StringBuilder();
		message.append(" ".repeat(diagnostic.offsetInLine()));
		message.append(red("|"));
		message.append(System.lineSeparator());
		message.append(" ".repeat(diagnostic.offsetInLine()));
		message.append(red("|"));
		message.append(System.lineSeparator());
		message.append(" ".repeat(diagnostic.offsetInLine()));
		message.append(red("= "));
		message.append(red(diagnostic.severity().toString()));
		message.append(red(": "));
		message.append(red(diagnostic.message()));
		return message.toString();
	}

	private String readDiagnosticSourceLine(Path path, IDiagnostic diagnostic)
	{
		var source = filesystem.readFile(path);
		var split = source.split("\n");

		if (split.length < diagnostic.line())
		{
			throw new RuntimeException("File <%s> does not contain line number (0-based): %d".formatted(path, diagnostic.line()));
		}

		var line = split[diagnostic.line()];
		var coloredLine = new StringBuilder();
		for (var i = 0; i < line.length(); i++)
		{
			if (i == diagnostic.offsetInLine())
			{
				coloredLine.append((char) 27 + "[31m");
			}

			if (i == diagnostic.offsetInLine() + diagnostic.length())
			{
				coloredLine.append((char) 27 + "[0m");
			}

			coloredLine.append(line.charAt(i));
		}

		coloredLine.append((char) 27 + "[0m");
		return coloredLine.toString();
	}

	private String squiggle(IDiagnostic diagnostic)
	{
		return " ".repeat(Math.max(0, diagnostic.offsetInLine())) +
			red("~".repeat(Math.max(0, diagnostic.length())));
	}

	private String red(String message)
	{
		var coloredMessage = (char) 27 + "[31m";
		coloredMessage += message;
		coloredMessage += (char) 27 + "[0m";
		return coloredMessage;
	}
}
