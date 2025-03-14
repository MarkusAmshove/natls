package org.amshove.natqube.measures;

import org.amshove.natqube.NaturalMetrics;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;

public class FileTypeMeasure
{
	public void measure(SensorContext context, InputFile inputFile)
	{
		var filenameWithExtension = inputFile.filename();
		var extension = filenameWithExtension.substring(filenameWithExtension.lastIndexOf('.') + 1);
		var metric = switch (extension)
		{
			case "NSD" -> NaturalMetrics.NUMBER_OF_DDMS;
			case "NSN" -> NaturalMetrics.NUMBER_OF_SUBPROGRAMS;
			case "NSP" -> NaturalMetrics.NUMBER_OF_PROGRAMS;
			case "NSS" -> NaturalMetrics.NUMBER_OF_EXTERNAL_SUBROUTINES;
			case "NSH" -> NaturalMetrics.NUMBER_OF_HELPROUTINES;
			case "NSG" -> NaturalMetrics.NUMBER_OF_GDAS;
			case "NSL" -> NaturalMetrics.NUMBER_OF_LDAS;
			case "NSA" -> NaturalMetrics.NUMBER_OF_PDAS;
			case "NSM" -> NaturalMetrics.NUMBER_OF_MAPS;
			case "NSC" -> NaturalMetrics.NUMBER_OF_COPYCODES;
			case "NS7" -> NaturalMetrics.NUMBER_OF_FUNCTIONS;
			// TEXT
			// CLASS NS4
			// ADAPTER
			// RESOURCE
			default -> null;
		};

		if (metric == null)
		{
			return;
		}

		context
			.<Integer> newMeasure()
			.on(inputFile)
			.forMetric(metric)
			.withValue(1)
			.save();
	}
}
