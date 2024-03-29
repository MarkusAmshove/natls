package org.amshove.natqube.measures;

import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natqube.NaturalMetrics;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;

public class FileTypeMeasure implements INaturalFileMeasurer
{
	@Override
	public void measure(SensorContext context, NaturalFile file, InputFile inputFile)
	{
		var metric = switch (file.getFiletype())
		{
			case DDM -> NaturalMetrics.NUMBER_OF_DDMS;
			case SUBPROGRAM -> NaturalMetrics.NUMBER_OF_SUBPROGRAMS;
			case PROGRAM -> NaturalMetrics.NUMBER_OF_PROGRAMS;
			case SUBROUTINE -> NaturalMetrics.NUMBER_OF_EXTERNAL_SUBROUTINES;
			case HELPROUTINE -> NaturalMetrics.NUMBER_OF_HELPROUTINES;
			case GDA -> NaturalMetrics.NUMBER_OF_GDAS;
			case LDA -> NaturalMetrics.NUMBER_OF_LDAS;
			case PDA -> NaturalMetrics.NUMBER_OF_PDAS;
			case MAP -> NaturalMetrics.NUMBER_OF_MAPS;
			case COPYCODE -> NaturalMetrics.NUMBER_OF_COPYCODES;
			case FUNCTION -> NaturalMetrics.NUMBER_OF_FUNCTIONS;
		};

		context
			.<Integer> newMeasure()
			.on(inputFile)
			.forMetric(metric)
			.withValue(1)
			.save();
	}
}
