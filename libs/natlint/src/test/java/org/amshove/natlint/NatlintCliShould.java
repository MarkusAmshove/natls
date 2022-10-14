package org.amshove.natlint;

import org.amshove.natlint.cli.AnalyzeCommand;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.IntegrationTest;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@IntegrationTest
class NatlintCliShould
{
	private PrintStream previousStdOut;
	private PrintStream previousStdErr;

	@Test
	void returnNonZeroExitCodeWhenDiagnosticsAreFound(@ProjectName("clitest") NaturalProject project)
	{
		var result = runNatlint("-w", project.getRootPath().toAbsolutePath().toString());
		assertThat(result.exitCode).isPositive();
	}

	private CliResult runNatlint(String... args)
	{
		var stdOutStream = new ByteArrayOutputStream();
		System.setOut(new PrintStream(stdOutStream));
		var stdErrStream = new ByteArrayOutputStream();
		System.setErr(new PrintStream(stdErrStream));

		var exitCode = new CommandLine(new AnalyzeCommand()).execute(args);

		return new CliResult(
			exitCode,
			stdOutStream.toString(StandardCharsets.UTF_8),
			stdErrStream.toString(StandardCharsets.UTF_8));
	}

	record CliResult(int exitCode, String stdOut, String stdErr)
	{
	}

	@BeforeEach
	void beforeEach()
	{
		previousStdOut = System.out;
		previousStdErr = System.err;

	}

	@AfterEach
	void afterEach()
	{
		System.setOut(previousStdOut);
		System.setErr(previousStdErr);
	}
}
