package org.amshove.natqube;

import org.amshove.natqube.sensor.NatlintSensor;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;

import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class NatlintSensorShould extends SonarQubeTest
{
	@Test
	void addAnIssue()
	{
		addNaturalFile("SUB.NSN", "DEFINE DATA LOCAL%n1 #A (A1)%nEND-DEFINE%n".formatted());

		var diagnosticFile = new TestInputFileBuilder(projectKey, "natlint/diagnostics-1.csv")
			.setProjectBaseDir(projectPath)
			.setModuleBaseDir(projectPath)
			.setContents(
				"file;ruleId;severity;message;line;offset;length%n%s;%s;%s;%s;%d;%d;%d%n".formatted(
					projectPath.resolve(Path.of("Natural-Libraries/LIB/SRC/SUB.NSN")),
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

		assertThat(context.allAnalysisErrors())
			.as("Expected no analysis errors")
			.isEmpty();

		assertThat(context.fileSystem().files(f -> true))
			.hasSize(2);

		assertThat(context.allIssues())
			.as("Number of all issues in project <%s> mismatches", projectPath)
			.hasSize(1);
	}
}
