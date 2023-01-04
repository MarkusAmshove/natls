package org.amshove.natqube.sensor;

import org.amshove.natqube.Natural;
import org.amshove.natqube.NaturalProperties;
import org.amshove.natqube.rules.NaturalRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.api.rule.RuleKey;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Collectors;

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
		var maybeDiagnosticFileName = config.get(NaturalProperties.NATLINT_ISSUE_FILE_KEY);
		if (maybeDiagnosticFileName.isEmpty())
		{
			LOGGER.error("No diagnostic file pattern found");
			return;
		}

		var diagnosticFile = context.fileSystem().inputFile((f) -> f.filename().equals(maybeDiagnosticFileName.get()));
		if (diagnosticFile == null)
		{
			LOGGER.error("Diagnostic file not found");
			return;
		}

		try
		{
			var diagnostics = diagnosticFile.contents().lines().skip(1).map(l ->
			{
				var split = l.split(";");
				if (split.length != 7)
				{
					return null;
				}
				var absolutePath = Path.of(split[0]).toUri();
				var id = split[1];
				var line = split[4];
				var offset = split[5];
				var length = split[6];
				var message = split[3];
				return new CsvDiagnostic(id, absolutePath, Integer.parseInt(line), Integer.parseInt(offset), Integer.parseInt(length), message);
			})
				.filter(Objects::nonNull)
				.collect(Collectors.groupingBy(CsvDiagnostic::getFileUri));

			context.fileSystem().inputFiles(f -> !f.filename().endsWith(".NSM")).forEach(f ->
			{
				if (!diagnostics.containsKey(f.uri()))
				{
					return;
				}

				var fileDiagnostics = diagnostics.get(f.uri());
				for (var diagnostic : fileDiagnostics)
				{
					try
					{
						var issue = context.newIssue();
						issue.at(
							issue.newLocation()
								.on(f)
								.at(f.newRange(diagnostic.getLine(), diagnostic.getOffsetInLine(), diagnostic.getLine(), diagnostic.getOffsetInLine() + diagnostic.getLength()))
								.message(diagnostic.getMessage())
						);
						issue
							.forRule(RuleKey.of(NaturalRuleRepository.REPOSITORY, diagnostic.getId()))
							.save();
					}
					catch (Exception e)
					{}
				}
			});
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
