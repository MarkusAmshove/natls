package org.amshove.natlint;

import org.amshove.natlint.linter.NaturalLinter;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.IPosition;
import org.amshove.natparse.infrastructure.ActualFilesystem;
import org.amshove.natparse.infrastructure.IFilesystem;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.natural.project.NaturalProjectFileIndexer;
import org.amshove.natparse.parsing.NaturalParser;
import org.amshove.natparse.parsing.project.BuildFileProjectReader;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class App
{
	private static String singleFile; // TODO: Implement proper
	private static String singleFolder;
	private static boolean noWarn;
	private static boolean printStatistic;
	private static String onlyDiagId;
	private static PathMatcher globPattern;

	private final Path projectFile;
	private final IFilesystem filesystem;

	public App(Path projectFile, IFilesystem filesystem)
	{
		this.projectFile = projectFile;
		this.filesystem = filesystem;
	}

	public static void main(String[] args) throws IOException
	{
		var arguments = new ArrayList<>(Arrays.stream(args).toList());

		var workingDirectoryPath = System.getProperty("user.dir");
		var workingDirectory = Paths.get(workingDirectoryPath);
		var filesystem = new ActualFilesystem();

		while (!workingDirectory.getRoot().equals(workingDirectory) && filesystem.findNaturalProjectFile(workingDirectory).isEmpty())
		{
			workingDirectory = workingDirectory.getParent();
		}

		var projectFile = filesystem.findNaturalProjectFile(workingDirectory);

		if (workingDirectory.getRoot().equals(workingDirectory) || projectFile.isEmpty())
		{
			throw new RuntimeException("Project root could not be determined. .natural or _naturalBuild file not found");
		}

		var folderIndex = arguments.indexOf("--folder");
		if(arguments.remove("--folder"))
		{
			singleFolder = arguments.get(folderIndex);
			arguments.remove(folderIndex);
		}

		var singleIndex = arguments.indexOf("--single");
		if(arguments.remove("--single"))
		{
			singleFile = arguments.get(singleIndex);
			arguments.remove(singleIndex);
		}
		noWarn = arguments.remove("--no-warn");
		printStatistic = arguments.remove("--statistic");

		var diagIdIndex = arguments.indexOf("--diag");
		if(arguments.remove("--diag"))
		{
			onlyDiagId = arguments.get(diagIdIndex);
			arguments.remove(diagIdIndex);
		}

		var globIndex = arguments.indexOf("--glob");
		if(arguments.remove("--glob"))
		{
			globPattern = FileSystems.getDefault().getPathMatcher("glob:"+arguments.get(globIndex));
			arguments.remove(globIndex);
		}

		if(!arguments.isEmpty())
		{
			System.out.println("Dangling arguments:");
			arguments.forEach(System.out::println);
			System.exit(1);
		}

		System.out.printf("""
			     .@@@@@@@@@@@@@@@&
			    /@@@@@@@@@@@@@@@@@.          %s
			     @@@@@@@@*@@@@@@@@           Version: %s
			  ....###############            Project file: %s
			......###.@/##.@.####......
			     .###############
			     .###############
			     .###############
			       ############.
			           ....
			%n""", App.class.getPackage().getImplementationTitle(), App.class.getPackage().getImplementationVersion(), projectFile.get().getFileName());

		new App(projectFile.get(), filesystem).run();
	}

	private static final Comparator<IDiagnostic> byLineNumber = Comparator.comparingInt(IPosition::line);

	private record DiagnosticByCount(String message, int count)
	{
	}

	public void run()
	{
		var singleLib = singleFile != null ? singleFile.split("\\.")[0] : null; // TODO: Implement proper
		var singleModule = singleFile != null ? singleFile.split("\\.")[1] : null; // TODO: Implement proper
		if(singleFolder != null)
		{
			singleLib = singleFolder.split("\\.")[0];
			singleFolder = singleFolder.split("\\.")[1];
		}

		var startIndex = System.currentTimeMillis();
		var project = new BuildFileProjectReader(filesystem).getNaturalProject(projectFile);
		var indexer = new NaturalProjectFileIndexer(filesystem);
		indexer.indexProject(project);
		var endIndex = System.currentTimeMillis();

		var lexer = new Lexer();
		var parser = new NaturalParser();
		var linter = new NaturalLinter();
		var diagnosticsPerType = new HashMap<String, Integer>();
		var filesChecked = 0;
		var totalDiagnostics = 0;
		var startCheck = System.currentTimeMillis();

		for (var library : project.getLibraries())
		{
			if(singleLib != null && !singleLib.equalsIgnoreCase(library.getName()))
			{
				continue;
			}

			for (var file : library.files().stream().filter(f -> f.getFiletype().canHaveDefineData()).toList())
			{
				if(globPattern != null && !globPattern.matches(file.getPath()))
				{
					continue;
				}

				if(singleModule != null && !singleModule.equalsIgnoreCase(file.getFilenameWithoutExtension()))
				{
					continue;
				}

				if(singleFolder != null && !file.getPath().toString().contains(singleFolder))
				{
					continue;
				}

				filesChecked++;
				var filePath = file.getPath();
				try
				{
					var tokens = lexer.lex(filesystem.readFile(filePath), filePath);
					var allDiagnostics = new ArrayList<>(tokens.diagnostics().toList());
					var module = parser.parse(file, tokens);
					allDiagnostics.addAll(module.diagnostics().toList());
					var linterDiagnostics = linter.lint(module);
					allDiagnostics.addAll(linterDiagnostics.toList());

					allDiagnostics.forEach(d -> {
						if(noWarn && (d.severity() == DiagnosticSeverity.WARNING || d.severity() == DiagnosticSeverity.INFO))
						{
							return;
						}

						if(onlyDiagId != null && !d.id().equalsIgnoreCase(onlyDiagId))
						{
							return;
						}

						var count = diagnosticsPerType.computeIfAbsent(d.message(), (k) -> 0);
						count++;
						diagnosticsPerType.replace(d.message(), count);
					});
					if(onlyDiagId != null)
					{
						totalDiagnostics += allDiagnostics.stream().filter(d -> d.id().equalsIgnoreCase(onlyDiagId)).count();
					}
					else
					{
						totalDiagnostics += allDiagnostics.size();
					}
					printDiagnostics(filePath, allDiagnostics);
				}
				catch (Exception e)
				{
					System.err.println(filePath);
					e.printStackTrace();
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

		if(printStatistic)
		{
			diagnosticsPerType.entrySet().stream().map((entry) -> new DiagnosticByCount(entry.getKey(), entry.getValue())).sorted(Comparator.comparingInt(DiagnosticByCount::count))
				.toList()
				.forEach(d -> System.out.println(d.message + "|" + d.count));
		}
	}

	private static final Map<DiagnosticSeverity, String> SEVERITY_COLOR_MAP = Map.of(
		DiagnosticSeverity.ERROR, "31",
		DiagnosticSeverity.WARNING, "33",
		DiagnosticSeverity.INFO, "34"
	);

	private void printDiagnostics(Path filePath, List<IDiagnostic> diagnostics)
	{
		if (diagnostics.isEmpty())
		{
			return;
		}

		var sortedDiagnostics = diagnostics.stream().sorted(byLineNumber).toList();

		var printed = 0;
		for (var diagnostic : sortedDiagnostics)
		{
			if((diagnostic.severity() == DiagnosticSeverity.WARNING || diagnostic.severity() == DiagnosticSeverity.INFO) && noWarn)
			{
				continue;
			}

			if(onlyDiagId != null && !diagnostic.id().equalsIgnoreCase(onlyDiagId))
			{
				continue;
			}

			System.out.println(pathWithLineInformation(diagnostic));

			System.out.println();
			System.out.println(readDiagnosticSourceLine(diagnostic));
			System.out.println(squiggle(diagnostic));
			System.out.println(message(diagnostic));
			System.out.println();

			printed++;
		}

		if(printed > 0)
		{
			System.out.println();
		}
	}

	private String message(IDiagnostic diagnostic)
	{
		var offsetInLine = diagnostic.originalPosition().isSamePositionAs(diagnostic) ? diagnostic.offsetInLine() : diagnostic.offsetInLine() + diagnostic.originalPosition().offsetInLine() + 2;
		var severity = diagnostic.severity();
		var message = new StringBuilder();
		message.append(" ".repeat(offsetInLine));
		message.append(colored("|", severity));
		message.append(System.lineSeparator());
		message.append(" ".repeat(offsetInLine));
		message.append(colored("|", severity));
		message.append(System.lineSeparator());
		message.append(" ".repeat(offsetInLine));
		message.append(colored("= ", severity));
		message.append(colored(diagnostic.severity().toString(), severity));
		message.append(colored(": ", severity));
		message.append(colored(splitMessage(diagnostic.message(), offsetInLine), severity));
		return message.toString();
	}

	private String splitMessage(String message, int offset)
	{
		var splitMessage = message.split("\n");
		return Arrays.stream(splitMessage).collect(Collectors.joining("\n" + " ".repeat(offset)));
	}

	private String readDiagnosticSourceLine(IDiagnostic diagnostic)
	{
		var diagnosticLocationLine = readSourcePosition(diagnostic, diagnostic.severity());
		if(!diagnostic.originalPosition().filePath().equals(diagnostic.filePath()))
		{
			var originalLocationLine = readSourcePosition(diagnostic.originalPosition(), diagnostic.severity());
			return new StringBuilder()
				.append(diagnosticLocationLine)
				.append("\n")
				.append(" ".repeat(diagnostic.offsetInLine()))
				.append(colored("^\n", diagnostic.severity()))
				.append(" ".repeat(diagnostic.offsetInLine()))
				.append(colored("|\n", diagnostic.severity()))
				.append(" ".repeat(diagnostic.offsetInLine()))
				.append(pathWithLineInformation(diagnostic.originalPosition()))
				.append("\n")
				.append(" ".repeat(diagnostic.offsetInLine()))
				.append(colored("|\n", diagnostic.severity()))
				.append(" ".repeat(diagnostic.offsetInLine()))
				.append(colored("= ", diagnostic.severity()))
				.append(originalLocationLine)
				.toString();
		}

		return diagnosticLocationLine;
	}

	private String readSourcePosition(IPosition position, DiagnosticSeverity severity)
	{
		var source = filesystem.readFile(position.filePath());
		var split = source.split("\n");

		if (split.length < position.line())
		{
			throw new RuntimeException("File <%s> does not contain line number (0-based): %d".formatted(position.filePath(), position.line()));
		}

		var line = split[position.line()];
		var coloredLine = new StringBuilder();
		for (var i = 0; i < line.length(); i++)
		{
			if (i == position.offsetInLine())
			{
				coloredLine
					.append((char) 27 + "[")
					.append(SEVERITY_COLOR_MAP.get(severity))
					.append("m");
			}

			if (i == position.offsetInLine() + position.length())
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
		var offsetInLine = diagnostic.originalPosition().isSamePositionAs(diagnostic) ? diagnostic.offsetInLine() : diagnostic.offsetInLine() + diagnostic.originalPosition().offsetInLine() + 2;
		return " ".repeat(Math.max(0, offsetInLine)) +
			colored("~".repeat(Math.max(0, diagnostic.originalPosition().length())), diagnostic.severity());
	}

	private String colored(String message, DiagnosticSeverity severity)
	{
		var coloredMessage = (char) 27 + "[" + SEVERITY_COLOR_MAP.get(severity) + "m";
		coloredMessage += message;
		coloredMessage += (char) 27 + "[0m";
		return coloredMessage;
	}

	private String pathWithLineInformation(IPosition position)
	{
		return "%s:%d:%d".formatted(position.filePath(), position.line() + 1, position.offsetInLine());
	}
}
