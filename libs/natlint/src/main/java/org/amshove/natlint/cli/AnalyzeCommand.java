package org.amshove.natlint.cli;

import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.natural.project.NaturalFile;
import picocli.CommandLine;

import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

@CommandLine.Command(name = "analyze", description = "Analyze the Natural project in the current working directory", mixinStandardHelpOptions = true)
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

	@Override
	public Integer call()
	{
		var modulePredicates = new ArrayList<Predicate<NaturalFile>>();
		var diagnosticPredicates = new ArrayList<Predicate<IDiagnostic>>();

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

		diagnosticPredicates.add(d -> d.severity().isWorseOrEqualTo(minimumSeverity));

		if (diagnosticIds != null)
		{
			diagnosticIds
				.forEach(id -> diagnosticPredicates.add(d -> d.id().equals(id)));
		}

		if (ciMode)
		{
			sinkType = DiagnosticSinkType.CI_CSV;
		}
		var workingDirectoryPath = workingDirectory != null ? workingDirectory : System.getProperty("user.dir");
		var workingDirectory = Paths.get(workingDirectoryPath);

		var analyzer = new CliAnalyzer(
			workingDirectory,
			sinkType.createSink(),
			fileStatusMode ? FileStatusSink.create() : FileStatusSink.dummy(),
			modulePredicates.isEmpty() ? DEFAULT_MODULE_PREDICATES : modulePredicates,
			diagnosticPredicates.isEmpty() ? DEFAULT_DIAGNOSTIC_PREDICATES : diagnosticPredicates,
			disableLinting
		);

		var exitCode = analyzer.run();
		if (ciMode)
		{
			return 0;
		}

		return exitCode;
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
