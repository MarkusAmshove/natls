package org.amshove.natqube.measures;

import org.amshove.natparse.natural.project.NaturalFile;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;

public interface INaturalFileMeasurer
{
	void measure(SensorContext context, NaturalFile file, InputFile inputFile);
}
