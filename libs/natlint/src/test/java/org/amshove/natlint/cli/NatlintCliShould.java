package org.amshove.natlint.cli;

import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class NatlintCliShould extends CliTest
{
	@Test
	void returnNonZeroExitCodeWhenDiagnosticsAreFound(@ProjectName("clitest") NaturalProject project)
	{
		var result = runNatlint("-w", project.getRootPath().toAbsolutePath().toString());
		assertThat(result.exitCode()).isPositive();
	}
}
