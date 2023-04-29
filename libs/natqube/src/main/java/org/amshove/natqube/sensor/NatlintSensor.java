package org.amshove.natqube.sensor;

import org.amshove.natlint.linter.NaturalLinter;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.infrastructure.ActualFilesystem;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.natural.project.NaturalProjectFileIndexer;
import org.amshove.natparse.parsing.NaturalParser;
import org.amshove.natparse.parsing.project.BuildFileProjectReader;
import org.amshove.natqube.Natural;
import org.amshove.natqube.rules.NaturalRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.api.rule.RuleKey;

public class NatlintSensor implements Sensor
{
	private static final Logger LOGGER = LoggerFactory.getLogger(NatlintSensor.class);
	private final Configuration config;

	public NatlintSensor(Configuration config)
	{

		this.config = config;
	}

	@Override
	public void describe(SensorDescriptor descriptor)
	{
		descriptor.name("Natlint Sensor");
		descriptor.onlyOnLanguage(Natural.KEY);
	}

	@Override
	public void execute(SensorContext context)
	{
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

					var inputFile = context.fileSystem().inputFile(f -> f.uri().equals(naturalFile.getPath().toUri()));
					if (inputFile == null)
					{
						throw new RuntimeException("Couldn't find input file for natural file");
					}

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
				}
				catch (Exception e)
				{
					LOGGER.error("Error on %s".formatted(naturalFile.getPath()), e);
				}
			});
		}
		var analysisEnd = System.currentTimeMillis();
		LOGGER.error("Analysis ended after %dms".formatted(analysisEnd - analysisStart));
	}

	private void saveDiagnosticAsIssue(SensorContext context, InputFile inputFile, IDiagnostic diagnostic)
	{
		var issue = context.newIssue();
		issue.at(
			issue.newLocation()
				.on(inputFile)
				.at(inputFile.newRange(diagnostic.line() + 1, diagnostic.offsetInLine(), diagnostic.line() + 1, diagnostic.endOffset()))
				.message(diagnostic.message())
		);
		issue
			.forRule(RuleKey.of(NaturalRuleRepository.REPOSITORY, diagnostic.id()))
			.save();
	}
}
