package org.amshove.natqube;

import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalProject;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.Metric;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Fail.fail;

public class TestContext
{
	public static final String MODULE_KEY = "moduleskey";
	private final SensorContextTester context;
	private final Path directory;
	private final NaturalProject project;

	public static TestContext fromProject(NaturalProject project)
	{
		return new TestContext(project);
	}

	public <T extends Serializable> void assertMeasure(InputFile file, Metric<T> metricKey, T value)
	{
		var measure = context.measure(file.key(), metricKey);

		if (measure == null && value == null)
		{
			return;
		}

		if (measure == null)
		{
			fail("No Measure <" + metricKey.getName() + "> on " + file.filename());
		}

		assertThat(value).as("Metric <" + metricKey.getName() + "> did not match").isEqualTo(measure.value());
	}

	public NaturalFile findNaturalFile(String lib, String module)
	{
		for (var library : project.getLibraries())
		{
			if (library.getName().equalsIgnoreCase(lib))
			{
				for (var file : library.files())
				{
					if (file.getReferableName().equalsIgnoreCase(module))
					{
						return file;
					}
				}
			}
		}

		throw new RuntimeException("Natural file %s.%s not found".formatted(lib, module));
	}

	public SensorContext sensorContext()
	{
		return context;
	}

	public InputFile findInputFile(String lib, String module)
	{
		var naturalFile = findNaturalFile(lib, module);
		return context.fileSystem().inputFile(f -> f.key().equals(moduleKey(naturalFile)));
	}

	private String moduleKey(NaturalFile file)
	{
		return "%s:%s".formatted(MODULE_KEY, project.getRootPath().relativize(file.getPath()).toString()).replace("\\", "/");
	}

	private TestContext(NaturalProject project)
	{
		this.project = project;
		this.directory = project.getRootPath();
		context = SensorContextTester.create(this.directory);
		initializeContext();
	}

	private void initializeContext()
	{
		project.getLibraries().forEach(lib -> lib.files().forEach(file ->
		{
			var inputFile = TestInputFileBuilder
				.create(MODULE_KEY, project.getRootPath().relativize(file.getPath()).toString())
				.setContents(fileContents(file.getPath()))
				.build();

			context.fileSystem().add(inputFile);
		}));
	}

	private String fileContents(Path filePath)
	{
		try
		{
			return Files.readString(filePath, StandardCharsets.UTF_8);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}
}
