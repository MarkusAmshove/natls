package org.amshove.natlint.cli;

import org.amshove.natlint.cli.git.GitStatusPredicateParser;
import org.amshove.natlint.cli.sinks.FileStatusSink;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.natural.project.NaturalFile;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

@CommandLine.Command(name = "analyze", description = "Analyze the Natural project in the current working directory", mixinStandardHelpOptions = true)
@SuppressWarnings("java:S106")
public class AnalyzeCommand implements Callable<Integer>
{
	@CommandLine.Option(names =
	{
		"-w", "--workdir"
	}, description = "Sets the working directory to a different path than the current one")
	String workingDirectory;

	@CommandLine.Option(names =
	{
		"-f", "--file"
	}, description = "Only analyze modules matching any of the qualified module name in the form of LIBRARY.MODULENAME (e.g. LIB1.SUBPROG)")
	List<String> qualifiedNames;

	@CommandLine.Option(names =
	{
		"-r", "--relative"
	}, description = "Only analyze modules matching any of the relative paths. Path should be relative to project root.")
	List<String> relativePaths;

	@CommandLine.Option(names =
	{
		"-l", "--library"
	}, description = "Only analyze modules that reside in any of the given libraries.")
	List<String> libraries;

	@CommandLine.Option(names =
	{
		"-g", "--glob"
	}, description = "Only analyze modules that match the given glob pattern.")
	List<String> globs;

	@CommandLine.Option(names =
	{
		"-s", "--severity"
	}, description = "Filter out diagnostics that are below the given severity. Valid values: ${COMPLETION-CANDIDATES}", defaultValue = "INFO")
	DiagnosticSeverity minimumSeverity;

	@CommandLine.Option(names =
	{
		"-d", "--diagnostic"
	}, description = "Filter out every diagnostic that does not match the given id. Example: --diagnostic NLP011")
	List<String> diagnosticIds;

	@CommandLine.Option(names =
	{
		"--sink"
	}, description = "Sets the output sink where the diagnostics are printed to. Defaults to STDOUT. Valid values: ${COMPLETION-CANDIDATES}", defaultValue = "STDOUT")
	DiagnosticSinkType sinkType;

	@CommandLine.Option(names =
	{
		"--ci"
	}, description = "Analyzer will return exit code 0, even when diagnostics are found. Will also use the CSV sink", defaultValue = "false")
	boolean ciMode;

	@CommandLine.Option(names =
	{
		"--fs"
	}, description = "Analyzer will create a csv with file statuses", defaultValue = "false")
	boolean fileStatusMode;

	@CommandLine.Option(names =
	{
		"--disable-linting", "-xlint"
	}, description = "Skips analyzing with natlint", defaultValue = "false")
	boolean disableLinting;

	private static final List<Predicate<NaturalFile>> DEFAULT_MODULE_PREDICATES = List.of(f -> true);
	private static final List<Predicate<IDiagnostic>> DEFAULT_DIAGNOSTIC_PREDICATES = List.of(d -> true);

	private final List<Predicate<NaturalFile>> modulePredicates = new ArrayList<>();
	private final List<Predicate<IDiagnostic>> diagnosticPredicates = new ArrayList<>();

	@Override
	public Integer call()
	{
		configureModulePredicates();
		configureDiagnosticPredicates();
		configureSinkType();
		var analyzer = createAnalyzer();
		var exitCode = analyzer.run();
		return handleExitCode(exitCode);
	}

	@CommandLine.Command(name = "git-status", description = "Analyze files from `git status --porcelain`", mixinStandardHelpOptions = true)
	@SuppressWarnings("unused")
	public int analyzeFromGitStatus() throws IOException
	{
		configureModulePredicates();
		configureDiagnosticPredicates();
		configureSinkType();

		var br = new BufferedReader(new InputStreamReader(System.in));
		var gitChanges = new ArrayList<String>();
		var line = "";
		while ((line = br.readLine()) != null)
		{
			if (line.startsWith("On branch"))
			{
				System.err.printf("Unexpected line: %s%nDid you forget to run git status with --porcelain ?%n", line);
				printGitStatusUsage();
				return 1;
			}

			if (line.contains(".."))
			{
				System.err.printf("Path seems to be relative: %s%nDid you forget to run git status with --porcelain ?%n", line);
				printGitStatusUsage();
				return 1;
			}

			gitChanges.add(line);
		}

		var filePredicates = new GitStatusPredicateParser().parseStatusToPredicates(gitChanges);
		modulePredicates.addAll(filePredicates);

		var analyzer = createAnalyzer();
		var exitCode = analyzer.run();
		return handleExitCode(exitCode);
	}

	private void configureModulePredicates()
	{
		if (qualifiedNames != null)
		{
			qualifiedNames.stream()
				.map(QualifiedModuleName::from)
				.forEach(qn -> modulePredicates.add(f -> f.getFilenameWithoutExtension().equalsIgnoreCase(qn.filename) && f.getLibrary().getName().equalsIgnoreCase(qn.library)));
		}

		if (relativePaths != null)
		{
			relativePaths.stream()
				.map(Paths::get)
				.forEach(p -> modulePredicates.add(f -> f.getProjectRelativePath().equals(p)));
		}

		if (libraries != null)
		{
			libraries
				.forEach(l -> modulePredicates.add(f -> f.getLibrary().getName().equalsIgnoreCase(l)));
		}

		if (globs != null)
		{
			globs.stream()
				.map(g -> FileSystems.getDefault().getPathMatcher("glob:" + g))
				.forEach(gp -> modulePredicates.add(f -> gp.matches(f.getPath())));
		}
	}

	private void configureDiagnosticPredicates()
	{
		diagnosticPredicates.add(d -> d.severity().isWorseOrEqualTo(minimumSeverity));

		if (diagnosticIds != null)
		{
			diagnosticIds
				.forEach(id -> diagnosticPredicates.add(d -> d.id().equals(id)));
		}
	}

	private void configureSinkType()
	{
		if (ciMode)
		{
			sinkType = DiagnosticSinkType.CSV;
		}
	}

	private CliAnalyzer createAnalyzer()
	{
		var workingDirectoryPath = workingDirectory != null ? workingDirectory : System.getProperty("user.dir");
		var theWorkingDirectory = Paths.get(workingDirectoryPath);

		return new CliAnalyzer(
			theWorkingDirectory,
			sinkType.createSink(theWorkingDirectory),
			fileStatusMode ? FileStatusSink.create() : FileStatusSink.dummy(),
			modulePredicates.isEmpty() ? DEFAULT_MODULE_PREDICATES : modulePredicates,
			diagnosticPredicates.isEmpty() ? DEFAULT_DIAGNOSTIC_PREDICATES : diagnosticPredicates,
			disableLinting
		);
	}

	private int handleExitCode(int exitCode)
	{
		return ciMode ? 0 : exitCode;
	}

	private static void printGitStatusUsage()
	{
		System.out.println();
		System.out.println("Usage:");
		System.out.println("git status --porcelain | java -jar natlint.jar git-status");
	}

	record QualifiedModuleName(String library, String filename)
	{
		static QualifiedModuleName from(String qualifiedName)
		{
			var split = qualifiedName.split("\\.");
			if (split.length != 2)
			{
				throw new RuntimeException("Qualified names must contain a dot and be in the form of LIBRARY.MODULENAME");
			}
			return new QualifiedModuleName(split[0], split[1]);
		}
	}
}
