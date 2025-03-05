package org.amshove.natqube.sensor;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.amshove.natqube.Natural;
import org.amshove.natqube.rules.NaturalRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.rule.RuleKey;

public class NatlintSensor implements Sensor
{
	private static final Logger LOGGER = LoggerFactory.getLogger(NatlintSensor.class);

	@Override
	public void describe(SensorDescriptor descriptor)
	{
		descriptor.name("Natlint Sensor");
		descriptor.onlyOnLanguage(Natural.KEY);
	}

	@Override
	public void execute(SensorContext context)
	{
		var diagnosticFiles = context.fileSystem().inputFiles(f -> f.filename().matches("natqube-diagnostics-\\d+\\.csv"));

		var inputFileByUri = new HashMap<URI, InputFile>();
		context.fileSystem().inputFiles(f -> true).forEach(f -> inputFileByUri.put(f.uri(), f));

		for (var diagnosticFile : diagnosticFiles)
		{
			LOGGER.info("Processing diagnostics file %s".formatted(diagnosticFile.filename()));

			var diagnostics = readDiagnostics(diagnosticFile);
			if (diagnostics.isEmpty())
			{
				LOGGER.warn("No diagnostics found in file %s".formatted(diagnosticFile.filename()));
				continue;
			}

			for (var entry : diagnostics.entrySet())
			{
				var inputFile = inputFileByUri.get(entry.getKey());
				if (inputFile == null)
				{
					LOGGER.warn("Could not find input file for URI %s".formatted(entry.getKey()));
					continue;
				}

				for (var diagnostic : entry.getValue())
				{
					saveDiagnosticAsIssue(context, inputFile, diagnostic);
				}
			}
		}
	}

	private void saveDiagnosticAsIssue(SensorContext context, InputFile inputFile, CsvDiagnostic diagnostic)
	{
		var issue = context.newIssue();

		try
		{
			issue.at(
				issue.newLocation()
					.on(inputFile)
					.at(
						inputFile.newRange(
							diagnostic.getLine(), diagnostic.getOffsetInLine(), diagnostic.getLine(),
							diagnostic.getOffsetInLine() + diagnostic.getLength()
						)
					)
					.message(diagnostic.getMessage())
			);

			issue
				.forRule(RuleKey.of(NaturalRuleRepository.REPOSITORY, diagnostic.getId()))
				.save();
		}
		catch (Exception e)
		{
			LOGGER.error("Error while saving diagnostic in file %s".formatted(inputFile.uri()), e);
		}
	}

	private static Map<URI, List<CsvDiagnostic>> readDiagnostics(InputFile diagnosticFile)
	{
		try
		{
			return diagnosticFile.contents().lines().skip(1).map(l ->
			{
				var split = l.split(";");
				// TODO: Magic numbers
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
				return new CsvDiagnostic(
					id, absolutePath, Integer.parseInt(line), Integer.parseInt(offset),
					Integer.parseInt(length), message
				);
			})
				.filter(Objects::nonNull)
				.collect(Collectors.groupingBy(CsvDiagnostic::getFileUri));
		}
		catch (IOException e)
		{
			LOGGER.error("Could not read diagnostics from file %s".formatted(diagnosticFile.filename()), e);
			return Map.of();
		}
	}
}
