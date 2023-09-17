package org.amshove.natqube.sensor;

import org.amshove.natlint.linter.NaturalLinter;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.IPosition;
import org.amshove.natparse.infrastructure.ActualFilesystem;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalProjectFileIndexer;
import org.amshove.natparse.parsing.NaturalParser;
import org.amshove.natparse.parsing.project.BuildFileProjectReader;
import org.amshove.natqube.Natural;
import org.amshove.natqube.measures.FileTypeMeasure;
import org.amshove.natqube.rules.NaturalRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.api.rule.RuleKey;

import java.nio.file.Path;

public class NatlintSensor implements Sensor
{
	private static final Logger LOGGER = LoggerFactory.getLogger(NatlintSensor.class);
	private final Configuration config;
	private SensorContext sensorContext;

	public NatlintSensor(Configuration config)
	{

		this.config = config;
	}

	private long saveIssuesMaxTime = Long.MIN_VALUE;
	private NaturalFile saveIssuesMaxFile = null;
	private long longestSingleIssueSave = Long.MIN_VALUE;

	@Override
	public void describe(SensorDescriptor descriptor)
	{
		descriptor.name("Natlint Sensor");
		descriptor.onlyOnLanguage(Natural.KEY);
	}

	@Override
	public void execute(SensorContext context)
	{
		sensorContext = context;
		var filesystem = new ActualFilesystem();
		LOGGER.error("Finding project file");
		var buildfilePath = filesystem.findNaturalProjectFile(context.fileSystem().baseDir().toPath()).get();
		var project = new BuildFileProjectReader(filesystem).getNaturalProject(buildfilePath);
		LOGGER.error("Project initialized: %s".formatted(buildfilePath));
		LOGGER.error("Start indexing");
		var indexStartTime = System.currentTimeMillis();
		new NaturalProjectFileIndexer().indexProject(project);
		var indexEndTime = System.currentTimeMillis();
		LOGGER.error("Indexing done: %dms".formatted(indexEndTime - indexStartTime));

		var fileTypeMeasurer = new FileTypeMeasure();

		var analysisStart = System.currentTimeMillis();
		for (var library : project.getLibraries())
		{
			LOGGER.error("Starting lib %s".formatted(library.getName()));
			library.files().parallelStream().forEach(naturalFile ->
			{
				try
				{
					var lexResult = new Lexer().lex(filesystem.readFile(naturalFile.getPath()), naturalFile.getPath());
					var parseResult = new NaturalParser().parse(naturalFile, lexResult);
					var lintResult = new NaturalLinter().lint(parseResult);

					var inputFile = findInputFile(naturalFile.getPath());
					if (inputFile == null)
					{
						throw new RuntimeException("Couldn't find input file for natural file");
					}

					fileTypeMeasurer.measure(context, naturalFile, inputFile);

					var saveIssuesStart = System.currentTimeMillis();
					for (var diagnostic : lexResult.diagnostics())
					{
						saveDiagnosticAsIssue(context, inputFile, diagnostic);
					}
					for (var diagnostic : parseResult.diagnostics())
					{
						saveDiagnosticAsIssue(context, inputFile, diagnostic);
					}
					for (var diagnostic : lintResult)
					{
						saveDiagnosticAsIssue(context, inputFile, diagnostic);
					}
					var saveIssuesDuration = System.currentTimeMillis() - saveIssuesStart;

					if (saveIssuesDuration > saveIssuesMaxTime)
					{
						saveIssuesMaxTime = saveIssuesDuration;
						saveIssuesMaxFile = naturalFile;
					}
				}
				catch (Exception e)
				{
					LOGGER.error("Error on %s".formatted(naturalFile.getPath()), e);
				}
			});
		}
		var analysisEnd = System.currentTimeMillis();
		LOGGER.error("Analysis ended after %dms".formatted(analysisEnd - analysisStart));

		LOGGER.error("Longest save issues: %dms, %s".formatted(saveIssuesMaxTime, saveIssuesMaxFile.getPath()));
		LOGGER.error("Longest single issue save: %dms".formatted(longestSingleIssueSave));
	}

	private void saveDiagnosticAsIssue(SensorContext context, InputFile inputFile, IDiagnostic diagnostic)
	{
		var issue = context.newIssue();
		issue.at(
			issue.newLocation()
				.on(inputFile)
				.at(textRange(inputFile, diagnostic))
				.message(diagnostic.message())
		);
		for (var info : diagnostic.additionalInfo())
		{
			var sonarFile = findInputFile(info.position().filePath());
			issue.addLocation(
				issue.newLocation()
					.on(sonarFile)
					.at(textRange(sonarFile, info.position()))
					.message(info.message())
			);
		}
		var saveStart = System.currentTimeMillis();
		issue
			.forRule(RuleKey.of(NaturalRuleRepository.REPOSITORY, diagnostic.id()))
			.save();
		var saveDuration = System.currentTimeMillis() - saveStart;
		if (saveDuration > longestSingleIssueSave)
		{
			longestSingleIssueSave = saveDuration;
		}
	}

	private InputFile findInputFile(Path filePath)
	{
		return sensorContext.fileSystem().inputFile(f -> f.uri().equals(filePath.toUri()));
	}

	private TextRange textRange(InputFile file, IPosition position)
	{
		return file.newRange(position.line() + 1, position.offsetInLine(), position.line() + 1, position.endOffset());
	}
}
