package org.amshove.natqube.measures;

import org.amshove.natqube.NaturalMetrics;
import org.amshove.natqube.NaturalModuleType;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;

public class FileTypeMeasure
{
	public void measure(SensorContext context, InputFile inputFile)
	{
		var naturalModuleType = NaturalModuleType.fromInputFile(inputFile);
		if (naturalModuleType == null)
		{
			return;
		}

		var metric = switch (naturalModuleType)
		{
			case DDM -> NaturalMetrics.NUMBER_OF_DDMS;
			case SUBPROGRAM -> NaturalMetrics.NUMBER_OF_SUBPROGRAMS;
			case PROGRAM -> NaturalMetrics.NUMBER_OF_PROGRAMS;
			case SUBROUTINE -> NaturalMetrics.NUMBER_OF_EXTERNAL_SUBROUTINES;
			case HELP_ROUTINE -> NaturalMetrics.NUMBER_OF_HELPROUTINES;
			case GDA -> NaturalMetrics.NUMBER_OF_GDAS;
			case LDA -> NaturalMetrics.NUMBER_OF_LDAS;
			case PDA -> NaturalMetrics.NUMBER_OF_PDAS;
			case MAP -> NaturalMetrics.NUMBER_OF_MAPS;
			case COPY_CODE -> NaturalMetrics.NUMBER_OF_COPYCODES;
			case FUNCTION -> NaturalMetrics.NUMBER_OF_FUNCTIONS;
			case TEXT -> NaturalMetrics.NUMBER_OF_TEXT_FILES;
			case CLASS -> NaturalMetrics.NUMBER_OF_CLASSES;
		};

		context
			.<Integer> newMeasure()
			.on(inputFile)
			.forMetric(metric)
			.withValue(1)
			.save();
	}
}
