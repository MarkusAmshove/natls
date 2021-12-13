package org.amshove.natlint;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.IPosition;
import org.amshove.natparse.infrastructure.ActualFilesystem;
import org.amshove.natparse.infrastructure.IFilesystem;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.natural.project.NaturalProjectFileIndexer;
import org.amshove.natparse.parsing.DefineDataParser;
import org.amshove.natparse.parsing.project.BuildFileProjectReader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

	public void run()
	{
		var startIndex = System.currentTimeMillis();
		var project = new BuildFileProjectReader(filesystem).getNaturalProject(projectFile);
		var indexer = new NaturalProjectFileIndexer(filesystem);
		indexer.indexProject(project);
		var endIndex = System.currentTimeMillis();

		var lexer = new Lexer();
		var parser = new DefineDataParser();
		var diagnostics = new ArrayList<IDiagnostic>();
		var filesChecked = 0;
		var totalDiagnostics = 0;
		var startCheck = System.currentTimeMillis();
		for (var library : project.getLibraries())
		{
			for (var file : library.files().stream().filter(f -> f.getFiletype().hasDefineData()).toList())
			{
				filesChecked++;
				try
				{
					diagnostics.clear();
					var tokens = lexer.lex(filesystem.readFile(file.getPath()));
					diagnostics.addAll(tokens.diagnostics().stream().toList());

					var parseResult = parser.parse(tokens);
					diagnostics.addAll(parseResult.diagnostics().stream().toList());

					totalDiagnostics += diagnostics.size();
					printDiagnostics(file.getPath(), diagnostics);
				}
				catch(Exception e)
				{
					System.err.println(file.getPath());
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
	}

	private void printDiagnostics(Path filePath, List<IDiagnostic> diagnostics)
	{
		if (diagnostics.isEmpty())
		{
			return;
		}

		System.out.println(filePath);
		var sortedDiagnostics = diagnostics.stream().sorted(byLineNumber).toList();

		for (var diagnostic : sortedDiagnostics)
		{
			System.out.printf("%s:%s at %d:%d%n", diagnostic.severity(), diagnostic.id(), diagnostic.line(), diagnostic.offsetInLine());

			System.out.println();
			System.out.println(readDiagnosticSourceLine(filePath, diagnostic));
			System.out.println(squiggle(diagnostic));
			System.out.println(message(diagnostic));
			System.out.println();
		}

		System.out.println();
		System.out.println("Summary: ");
		diagnostics.stream()
			.collect(Collectors.groupingBy(IDiagnostic::severity))
			.forEach((severity, d) -> System.out.printf("%s: %d%n", severity, d.size()));
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
