package org.amshove.natlint.cli;

import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class SinkTests extends CliTest
{
	@Test
	void nullDiagnosticSinkShouldNotDoAnything(@ProjectName("clitest") NaturalProject project)
	{
		var result = runNatlint("-w", project.getRootPath().toAbsolutePath().toString(), "--sink=NONE");
		assertThat(result.stdOut()).doesNotContain("ERROR"); // no part of a diagnostic should be present
	}

	@Test
	void ansiDiagnosticSinkShouldPrintDiagnostics(@ProjectName("clitest") NaturalProject project)
	{
		var result = runNatlint("-w", project.getRootPath().toAbsolutePath().toString(), "--sink=STDOUT");
		assertThat(result.stdOut()).contains("ERROR"); // part of a diagnostic
	}

	@Test
	void csvDiagnosticSinkShouldCreateACsvFile(@ProjectName("clitest") NaturalProject project)
	{
		var result = runNatlint("-w", project.getRootPath().toAbsolutePath().toString(), "--sink=CSV");
		assertThat(result.stdOut()).doesNotContain("ERROR"); // no part of a diagnostic should be present
		assertThat(project.getRootPath().resolve("diagnostics.csv")).exists();
	}

	@Test
	void ciCsvDiagnosticSinkShouldCreateACsvFile(@ProjectName("clitest") NaturalProject project)
	{
		var result = runNatlint("-w", project.getRootPath().toAbsolutePath().toString(), "--ci");
		assertThat(result.stdOut()).doesNotContain("ERROR"); // no part of a diagnostic should be present
		assertThat(project.getRootPath().resolve("diagnostics.csv")).exists();
	}
}
