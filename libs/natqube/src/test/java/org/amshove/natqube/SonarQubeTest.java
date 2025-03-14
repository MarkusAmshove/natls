package org.amshove.natqube;

import java.io.Serializable;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.Metric;

public abstract class SonarQubeTest
{
	@TempDir
	protected Path projectPath;

	protected final String projectKey = "naturalProject";

	protected SensorContextTester context;

	@BeforeEach
	protected void setUp()
	{
		context = SensorContextTester.create(projectPath);
	}

	protected InputFile addNaturalFile(String filename, String content)
	{
		var file = new TestInputFileBuilder(projectKey, "Natural-Libraries/LIB/SRC/%s".formatted(filename))
			.setProjectBaseDir(projectPath)
			.setModuleBaseDir(projectPath)
			.setLanguage(Natural.KEY)
			.setContents(content)
			.build();

		context.fileSystem().add(file);
		return file;
	}

	protected <T extends Serializable> void assertMetric(Metric<T> metric, T value, InputComponent file)
	{
		var measures = context.measures(file.key());
		assertThat(measures)
			.as("Component should have any measures")
			.isNotEmpty();

		var foundMetric = measures.stream().filter(m -> m.metric().key().equals(metric.key())).findFirst();
		AssertionsForClassTypes.assertThat(foundMetric)
			.as("Metric %s should be present".formatted(metric.key()))
			.isPresent()
			.hasValueSatisfying(m -> AssertionsForClassTypes.assertThat(m.value()).isEqualTo(value));
	}
}
