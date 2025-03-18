package org.amshove.natqube;

import org.amshove.natqube.sensor.CsvDiagnostic;
import org.amshove.natqube.sensor.NatlintSensor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SuppressWarnings("deprecation")
class NatlintSensorShould extends SonarQubeTest
{
	@Test
	void addAnIssue()
	{
		var subprogram = addNaturalFile("SUB.NSN", "DEFINE DATA LOCAL%n1 #A (A1)%nEND-DEFINE%n".formatted());

		addDiagnostic(
			new CsvDiagnostic(
				"NL001",
				subprogram.relativePath(),
				1,
				2,
				2,
				"Variable #A is unused"
			)
		);

		var sensor = new NatlintSensor();
		sensor.execute(context);

		assertThat(context.allAnalysisErrors())
			.as("Expected no analysis errors")
			.isEmpty();

		assertThat(context.allIssues())
			.as("Number of all issues in project <%s> mismatches", projectPath)
			.hasSize(1);
	}
}
