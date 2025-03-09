package org.amshove.natqube;

import org.amshove.natqube.sensor.CsvDiagnostic;
import org.amshove.natqube.sensor.NatlintSensor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class NatlintSensorShould
{
	@TempDir
	Path projectDirectory;

	private final String projectKey = "projectKey";
	private SensorContextTester context;

	@BeforeEach
	void setupContext()
	{
		context = SensorContextTester.create(projectDirectory);
	}

	@Test
	void addAnIssue()
	{
		addNaturalFile("SUB.NSN", "DEFINE DATA LOCAL\n1 #A (A1)\nEND-DEFINE\n");

		var diagnosticFile = new TestInputFileBuilder(projectKey, "natlint/diagnostics-1.csv")
			.setProjectBaseDir(projectDirectory)
			.setModuleBaseDir(projectDirectory)
			.setContents(
				"file;ruleId;severity;message;line;offset;length\n"
					+ "%s;%s;%s;%s;%d;%d;%d\n".formatted(
						projectDirectory.resolve("Natural-Libraries/LIB/SRC/SUB.NSN"),
						"NL001",
						"WARNING",
						"Variable #A is unused",
						1,
						2,
						2
					)
			)
			.build();

		context.fileSystem().add(diagnosticFile);

		var sensor = new NatlintSensor();
		sensor.execute(context);

		assertThat(context.allIssues()).hasSize(1);
	}

	void addNaturalFile(String name, String content)
	{
		var file = new TestInputFileBuilder(projectKey, "Natural-Libraries/LIB/SRC/%s".formatted(name))
			.setProjectBaseDir(projectDirectory)
			.setModuleBaseDir(projectDirectory)
			.setLanguage(Natural.KEY)
			.setContents(content)
			.build();

		context.fileSystem().add(file);
	}
}
