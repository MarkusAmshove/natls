package org.amshove.natlint.cli;

import org.amshove.natlint.api.LinterDiagnostic;
import org.amshove.natlint.cli.FileStatusSink.MessageType;
import org.amshove.natlint.editorconfig.EditorConfigParser;
import org.amshove.natlint.linter.LinterContext;
import org.amshove.natlint.linter.NaturalLinter;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.infrastructure.ActualFilesystem;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.lexing.TokenList;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.project.*;
import org.amshove.natparse.parsing.NaturalParser;
import org.amshove.natparse.parsing.project.BuildFileProjectReader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

public class CliAnalyzer
{
	private final List<Predicate<NaturalFile>> filePredicates;
	private final List<Predicate<IDiagnostic>> diagnosticPredicates;
	private final ActualFilesystem filesystem;
	private final IDiagnosticSink diagnosticSink;
	private final boolean disableLinting;
	private Path workingDirectory;
	private final FileStatusSink fileStatusSink;

	public CliAnalyzer(Path workingDirectory, IDiagnosticSink sink, FileStatusSink fileStatusSink, List<Predicate<NaturalFile>> filePredicates, List<Predicate<IDiagnostic>> diagnosticPredicates, boolean disableLinting)
	{
		this.workingDirectory = workingDirectory;
		this.filePredicates = filePredicates;
		this.diagnosticPredicates = diagnosticPredicates;
		filesystem = new ActualFilesystem();
		diagnosticSink = sink;
		this.fileStatusSink = fileStatusSink;
		this.disableLinting = disableLinting;
	}

	public int run()
	{
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

		var editorconfigPath = projectFile.get().getParent().resolve(".editorconfig");
		if (editorconfigPath.toFile().exists())
		{
			LinterContext.INSTANCE.updateEditorConfig(new EditorConfigParser().parse(filesystem.readFile(editorconfigPath)));
		}

		System.out.printf(
			"""
			     .@@@@@@@@@@@@@@@&
			    /@@@@@@@@@@@@@@@@@.          %s
			     @@@@@@@@*@@@@@@@@           Version: %s
			  ....###############            Project file: %s
			......###.@/##.@.####......      %s
			     .###############
			     .###############
			     .###############
			       ############.
			           ....
			%n""",
			CliAnalyzer.class.getPackage().getImplementationTitle(),
			CliAnalyzer.class.getPackage().getImplementationVersion(),
			projectFile.get().getFileName(),
			editorconfigPath.toFile().exists() ? ".editorconfig picked up" : ""
		);

		return analyze(projectFile.get());
	}

	private SlowestModule slowestLexedModule = new SlowestModule(Long.MIN_VALUE, "NONE");
	private SlowestModule slowestParsedModule = new SlowestModule(Long.MIN_VALUE, "NONE");
	private SlowestModule slowestLintedModule = new SlowestModule(Long.MIN_VALUE, "NONE");
	private final AtomicInteger filesChecked = new AtomicInteger();
	private final AtomicInteger totalDiagnostics = new AtomicInteger();
	private final AtomicInteger exceptions = new AtomicInteger();
	private final AtomicLong linesOfCode = new AtomicLong();
	private long maxMemoryInBytes = 0L;

	private int analyze(Path projectFilePath)
	{
		var indexStartTime = System.currentTimeMillis();
		var project = new BuildFileProjectReader(filesystem).getNaturalProject(projectFilePath);
		new NaturalProjectFileIndexer().indexProject(project);
		var indexEndTime = System.currentTimeMillis();

		var startCheck = System.currentTimeMillis();
		for (var library : project.getLibraries())
		{
			library.files().parallelStream().forEach(file ->
			{
				if (file.getFiletype() == NaturalFileType.DDM)
				{
					// DdmParser isn't called in the CLI. DDMs will be parsed on demand.
					return;
				}

				if (file.isFailedOnInit())
				{
					fileStatusSink.printError(file.getPath(), MessageType.INDEX_EXCEPTION, file.getInitException());
					return;
				}
				if (filePredicates.stream().noneMatch(p -> p.test(file)))
				{
					fileStatusSink.printStatus(file.getPath(), MessageType.FILE_EXCLUDED);
					return;
				}

				filesChecked.incrementAndGet();
				var allDiagnosticsInFile = new ArrayList<IDiagnostic>();

				var tokens = lex(file, allDiagnosticsInFile);
				if (tokens == null)
				{
					return;
				}

				var module = parse(file, tokens, allDiagnosticsInFile);
				if (module == null)
				{
					diagnosticSink.printDiagnostics(filesChecked.get(), file.getPath(), allDiagnosticsInFile);
					return;
				}

				if (tokens.sourceHeader().getProgrammingMode() == NaturalProgrammingMode.REPORTING)
				{
					fileStatusSink.printStatus(file.getPath(), MessageType.REPORTING_TYPE);
					diagnosticSink.printDiagnostics(filesChecked.get(), file.getPath(), allDiagnosticsInFile);
					return;
				}

				if (!disableLinting && module.programmingMode() != NaturalProgrammingMode.REPORTING)
				{
					var linterDiagnostics = lint(file, module, allDiagnosticsInFile);
					if (linterDiagnostics == null)
					{
						return;
					}
				}

				totalDiagnostics.addAndGet(allDiagnosticsInFile.size());
				diagnosticSink.printDiagnostics(filesChecked.get(), file.getPath(), allDiagnosticsInFile);
				fileStatusSink.printStatus(file.getPath(), MessageType.SUCCESS);
			});
			var currentMemory = Runtime.getRuntime().totalMemory();
			if (currentMemory > maxMemoryInBytes)
			{
				maxMemoryInBytes = currentMemory;
			}
		}

		var endCheck = System.currentTimeMillis();

		var missingStartTime = System.currentTimeMillis();
		registerMissingFiles(project);
		var missingEndTime = System.currentTimeMillis();

		var indexTime = indexEndTime - indexStartTime;
		var checkTime = endCheck - startCheck;
		var missTime = missingEndTime - missingStartTime;
		var totalTime = indexTime + checkTime + missTime;

		var totalTimeSeconds = totalTime / 1000;
		System.out.println();
		System.out.println("Done.");
		System.out.printf("Index time: %d ms%n", indexTime);
		System.out.printf("Check time: %d ms%n", checkTime);
		System.out.printf("Miss time : %d ms%n", missTime);
		System.out.printf("Total: %d ms (%ds)%n", totalTime, totalTimeSeconds);
		System.out.println();
		System.out.printf("Files checked: %,d%n", filesChecked.get());
		System.out.printf("Lines of code: %,d%n", linesOfCode.get());
		System.out.printf("LoC/s: %,d%n", totalTimeSeconds > 0 ? (linesOfCode.get() / totalTimeSeconds) : linesOfCode.get());
		System.out.println();
		System.out.printf("Total diagnostics: %,d%n", totalDiagnostics.get());
		System.out.println("Exceptions: " + exceptions.get());
		System.out.println();
		System.out.println("Slowest lexed module: " + slowestLexedModule);
		System.out.println("Slowest parsed module: " + slowestParsedModule);
		System.out.println("Slowest linted module: " + (disableLinting ? "disabled" : slowestLintedModule));
		System.out.printf("Peak memory usage: %.2f Mib%n", maxMemoryInBytes / 1024.0 / 1024.0);

		System.out.println();

		return totalDiagnostics.get() > 0 ? 1 : 0;
	}

	private List<? extends IDiagnostic> filterDiagnostics(ReadOnlyList<? extends IDiagnostic> diagnostics)
	{
		return diagnostics.stream().filter(d -> diagnosticPredicates.stream().allMatch(p -> p.test(d))).toList();
	}

	private TokenList lex(NaturalFile file, ArrayList<IDiagnostic> allDiagnosticsInFile)
	{
		try
		{
			var lexer = new Lexer();
			var lexStart = System.currentTimeMillis();
			var tokens = lexer.lex(filesystem.readFile(file.getPath()), file.getPath());
			var lexEnd = System.currentTimeMillis();
			countLinesOfCode(tokens);
			if (slowestLexedModule.milliseconds < lexEnd - lexStart)
			{
				slowestLexedModule = new SlowestModule(lexEnd - lexStart, file.getProjectRelativePath().toString());
			}

			var diagnostics = filterDiagnostics(tokens.diagnostics());
			fileStatusSink.printDiagnostics(file.getPath(), MessageType.LEX_FAILED, diagnostics);
			allDiagnosticsInFile.addAll(diagnostics);
			return tokens;
		}
		catch (Exception e)
		{
			fileStatusSink.printError(file.getPath(), MessageType.LEX_EXCEPTION, e);
			exceptions.incrementAndGet();
			System.out.println(file.getPath());
			e.printStackTrace();
			return null;
		}
	}

	private void countLinesOfCode(TokenList tokens)
	{
		var previousLine = -1;
		var totalLines = 0;
		for (var token : tokens)
		{
			if (token.line() != previousLine)
			{
				totalLines++;
				previousLine = token.line();
			}
		}

		linesOfCode.addAndGet(totalLines);
	}

	private INaturalModule parse(NaturalFile file, TokenList tokens, ArrayList<IDiagnostic> allDiagnosticsInFile)
	{
		try
		{
			var parser = new NaturalParser();
			var parseStart = System.currentTimeMillis();
			var module = parser.parse(file, tokens);
			var parseEnd = System.currentTimeMillis();
			if (slowestParsedModule.milliseconds < parseEnd - parseStart)
			{
				slowestParsedModule = new SlowestModule(parseEnd - parseStart, file.getProjectRelativePath().toString());
			}

			var diagnostics = filterDiagnostics(module.diagnostics());
			fileStatusSink.printDiagnostics(file.getPath(), MessageType.PARSE_FAILED, diagnostics);
			allDiagnosticsInFile.addAll(diagnostics);
			return module;
		}
		catch (Exception e)
		{
			fileStatusSink.printError(file.getPath(), MessageType.PARSE_EXCEPTION, e);
			exceptions.incrementAndGet();
			System.out.println(file.getPath());
			e.printStackTrace();
			return null;
		}
	}

	private ReadOnlyList<LinterDiagnostic> lint(NaturalFile file, INaturalModule module, ArrayList<IDiagnostic> allDiagnosticsInFile)
	{
		try
		{
			var linter = new NaturalLinter();
			var lintStart = System.currentTimeMillis();
			var linterDiagnostics = linter.lint(module);
			var lintEnd = System.currentTimeMillis();
			if (slowestLintedModule.milliseconds < lintEnd - lintStart)
			{
				slowestLintedModule = new SlowestModule(lintEnd - lintStart, file.getProjectRelativePath().toString());
			}

			var diagnostics = filterDiagnostics(linterDiagnostics);
			fileStatusSink.printDiagnostics(file.getPath(), MessageType.LINT_FAILED, diagnostics);
			allDiagnosticsInFile.addAll(diagnostics);
			return linterDiagnostics;
		}
		catch (Exception e)
		{
			fileStatusSink.printError(file.getPath(), MessageType.LINT_EXCEPTION, e);
			exceptions.incrementAndGet();
			System.out.println(file.getPath());
			e.printStackTrace();
			return null;
		}
	}

	private void registerMissingFiles(NaturalProject project)
	{
		if (!fileStatusSink.isEnabled())
		{
			return;
		}

		System.err.println("Started registration of missing files");
		var root = project.getRootPath().resolve("Natural-Libraries");
		System.out.println("Root: " + root.toString());

		try (var stream = Files.walk(root))
		{
			stream
				.filter(path -> !Files.isDirectory(path))
				.forEach(path -> fileStatusSink.printStatus(path, MessageType.FILE_MISSING));
			System.err.println("Finished registration of missing files");
		}
		catch (Exception e)
		{
			System.err.println("Registration of missing files failed");
			e.printStackTrace();
		}
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
