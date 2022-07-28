package org.amshove.natlint.cli;

import org.amshove.natlint.linter.NaturalLinter;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.infrastructure.ActualFilesystem;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalProjectFileIndexer;
import org.amshove.natparse.parsing.NaturalParser;
import org.amshove.natparse.parsing.project.BuildFileProjectReader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class CliAnalyzer
{
	private final List<Predicate<NaturalFile>> filePredicates;
	private final List<Predicate<IDiagnostic>> diagnosticPredicates;
	private final ActualFilesystem filesystem;
	private final IDiagnosticSink diagnosticSink;

	public CliAnalyzer(IDiagnosticSink sink, List<Predicate<NaturalFile>> filePredicates, List<Predicate<IDiagnostic>> diagnosticPredicates)
	{
		this.filePredicates = filePredicates;
		this.diagnosticPredicates = diagnosticPredicates;
		filesystem = new ActualFilesystem();
		diagnosticSink = sink;
	}

	public int run()
	{
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
			%n""", CliAnalyzer.class.getPackage().getImplementationTitle(), CliAnalyzer.class.getPackage().getImplementationVersion(), projectFile.get().getFileName());

		return analyze(projectFile.get());
	}

	private int analyze(Path projectFilePath)
	{
		var indexStartTime = System.currentTimeMillis();
		var project = new BuildFileProjectReader(filesystem).getNaturalProject(projectFilePath);
		new NaturalProjectFileIndexer().indexProject(project);
		var indexEndTime = System.currentTimeMillis();

		var slowestLexedModule = new SlowestModule(Long.MIN_VALUE, "NONE");
		var slowestParsedModule = new SlowestModule(Long.MIN_VALUE, "NONE");
		var slowestLintedModule = new SlowestModule(Long.MIN_VALUE, "NONE");

		var lexer = new Lexer();
		var parser = new NaturalParser();
		var linter = new NaturalLinter();
		var filesChecked = 0;
		var totalDiagnostics = 0;
		var exceptions = 0;

		var startCheck = System.currentTimeMillis();
		for (var library : project.getLibraries())
		{
			for (var file : library.files())
			{
				if (filePredicates.stream().noneMatch(p -> p.test(file)))
				{
					continue;
				}

				filesChecked++;
				var filePath = file.getPath();
				try
				{
					var lexStart = System.currentTimeMillis();
					var tokens = lexer.lex(filesystem.readFile(filePath), filePath);
					var lexEnd = System.currentTimeMillis();
					if (slowestLexedModule.milliseconds < lexEnd - lexStart)
					{
						slowestLexedModule = new SlowestModule(lexEnd - lexStart, file.getProjectRelativePath().toString());
					}

					var allDiagnosticsInFile = new ArrayList<IDiagnostic>(filterDiagnostics(tokens.diagnostics()));

					var parseStart = System.currentTimeMillis();
					var module = parser.parse(file, tokens);
					var parseEnd = System.currentTimeMillis();
					if (slowestParsedModule.milliseconds < parseEnd - parseStart)
					{
						slowestParsedModule = new SlowestModule(parseEnd - parseStart, file.getProjectRelativePath().toString());
					}
					allDiagnosticsInFile.addAll(filterDiagnostics(module.diagnostics()));

					var lintStart = System.currentTimeMillis();
					var linterDiagnostics = linter.lint(module);
					var lintEnd = System.currentTimeMillis();
					if (slowestLintedModule.milliseconds < lintEnd - lintStart)
					{
						slowestLintedModule = new SlowestModule(lintEnd - lintStart, file.getProjectRelativePath().toString());
					}

					allDiagnosticsInFile.addAll(filterDiagnostics(linterDiagnostics));

					totalDiagnostics += allDiagnosticsInFile.size();

					diagnosticSink.printDiagnostics(filePath, allDiagnosticsInFile);
				}
				catch (Exception e)
				{
					exceptions++;
					System.err.println(filePath);
					e.printStackTrace();
				}
			}
		}

		var endCheck = System.currentTimeMillis();

		var indexTime = indexEndTime - indexStartTime;
		var checkTime = endCheck - startCheck;

		System.out.println();
		System.out.println("Done.");
		System.out.println("Index time: " + indexTime + " ms");
		System.out.println("Check time: " + checkTime + " ms");
		System.out.println("Total: " + (indexTime + checkTime) + " ms (" + (indexTime + checkTime / 1000) + "s)");
		System.out.println("Files checked: " + filesChecked);
		System.out.println("Total diagnostics: " + totalDiagnostics);
		System.out.println("Exceptions: " + exceptions);
		System.out.println("Slowest lexed module: " + slowestLexedModule);
		System.out.println("Slowest parsed module: " + slowestParsedModule);
		System.out.println("Slowest linted module: " + slowestLintedModule);

		System.out.println();

		return totalDiagnostics > 0 ? 1 : 0;
	}

	private List<? extends IDiagnostic> filterDiagnostics(ReadOnlyList<? extends IDiagnostic> diagnostics)
	{
		return diagnostics.stream().filter(d -> diagnosticPredicates.stream().anyMatch(p -> p.test(d))).toList();
	}

	record SlowestModule(long milliseconds, String module)
	{
		@Override
		public String toString()
		{
			return "%dms (%s)".formatted(milliseconds, module);
		}
	}
}