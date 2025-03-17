package org.amshove.natqube.sensor;

import org.amshove.natqube.Natural;
import org.amshove.natqube.measures.FileTypeMeasure;
import org.amshove.natqube.rules.NaturalRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.rule.RuleKey;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class NatlintSensor implements Sensor
{
	private static final Logger LOGGER = LoggerFactory.getLogger(NatlintSensor.class);
	private Map<URI, InputFile> naturalFilesByUri;

	@Override
	public void describe(SensorDescriptor descriptor)
	{
		descriptor.name("Natlint Sensor");
		descriptor.onlyOnLanguage(Natural.KEY);
	}

	@Override
	public void execute(SensorContext context)
	{
		var diagnosticFiles = new ArrayList<InputFile>();
		naturalFilesByUri = new HashMap<>();

		var fileTypeMeasure = new FileTypeMeasure();

		for (var inputFile : context.fileSystem().inputFiles(f -> true))
		{
			if (isInNatlintFolder(inputFile) && inputFile.filename().matches("diagnostics-\\d+\\.csv"))
			{
				var filename = inputFile.filename();
				LOGGER.info("Found diagnostic file: {}", filename);
				diagnosticFiles.add(inputFile);
				continue;
			}

			var language = inputFile.language();
			if (language != null && language.equals(Natural.KEY))
			{
				LOGGER.debug("Found natural file: {}", inputFile.uri());
				naturalFilesByUri.put(inputFile.uri(), inputFile);
				fileTypeMeasure.measure(context, inputFile);
			}
		}

		for (var diagnosticFile : diagnosticFiles)
		{
			processDiagnosticFile(diagnosticFile, context);
		}
	}

	private void processDiagnosticFile(InputFile diagnosticFile, SensorContext context)
	{
		var diagnosticFileName = diagnosticFile.filename();
		LOGGER.info("Processing diagnostics file {}", diagnosticFileName);

		var diagnostics = readDiagnostics(diagnosticFile);
		if (diagnostics.isEmpty())
		{
			LOGGER.warn("No diagnostics found in file {}", diagnosticFileName);
			return;
		}

		var diagnosticCount = 0;

		for (var entry : diagnostics.entrySet())
		{
			diagnosticCount += entry.getValue().size();
			var inputFile = naturalFilesByUri.get(entry.getKey());
			if (inputFile == null)
			{
				LOGGER.warn("Could not find input file for URI {}", entry.getKey());
				continue;
			}

			for (var diagnostic : entry.getValue())
			{
				saveDiagnosticAsIssue(context, inputFile, diagnostic);
			}
		}

		LOGGER.info("Processed {} diagnostics from file {}", diagnosticCount, diagnosticFileName);
	}

	private static boolean isInNatlintFolder(InputFile inputFile)
	{
		var filePath = Paths.get(inputFile.uri());
		return filePath.getParent().getFileName().toString().equals("natlint");
	}

	private void saveDiagnosticAsIssue(SensorContext context, InputFile inputFile, CsvDiagnostic diagnostic)
	{
		var issue = context.newIssue();

		try
		{
			var diagnosticLength = diagnostic.getLength();
			// SonarQube can't handle diagnostics with length 0, which we use for e.g. NPP040
			diagnosticLength = diagnosticLength == 0 ? 1 : diagnosticLength;
			issue.at(
				issue.newLocation()
					.on(inputFile)
					.at(
						inputFile.newRange(
							diagnostic.getLine(), diagnostic.getOffsetInLine(), diagnostic.getLine(),
							diagnostic.getOffsetInLine() + diagnosticLength
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
			LOGGER.error("Error while saving diagnostic in file {}", inputFile.uri());
			LOGGER.error("Diagnostic: {}", diagnostic, e);
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
				var absolutePath = URI.create(split[0]);
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
			LOGGER.error("Could not read diagnostics from file {}", diagnosticFile.filename(), e);
			return Map.of();
		}
	}
}
