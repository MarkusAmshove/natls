package org.amshove.natlint.cli;

import org.amshove.testhelpers.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import picocli.CommandLine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

@IntegrationTest
abstract class CliTest
{
	private PrintStream previousStdOut;
	private PrintStream previousStdErr;
	private InputStream previousStdIn;
	private InputStream configuredStdIn;

	record CliResult(int exitCode, String stdOut, String stdErr)
	{}

	protected CliResult runNatlint(String... args)
	{
		var stdOutStream = new ByteArrayOutputStream();
		System.setOut(new PrintStream(stdOutStream));
		var stdErrStream = new ByteArrayOutputStream();
		System.setErr(new PrintStream(stdErrStream));

		System.setIn(configuredStdIn);

		var exitCode = new CommandLine(new AnalyzeCommand()).execute(args);

		return new NatlintCliShould.CliResult(
			exitCode,
			stdOutStream.toString(StandardCharsets.UTF_8),
			stdErrStream.toString(StandardCharsets.UTF_8)
		);
	}

	@BeforeEach
	void beforeEach()
	{
		previousStdOut = System.out;
		previousStdErr = System.err;
		previousStdIn = System.in;
		configuredStdIn = previousStdIn;
	}

	@AfterEach
	void afterEach()
	{
		System.setOut(previousStdOut);
		System.setErr(previousStdErr);
		System.setIn(previousStdIn);
	}

	protected void setStdIn(String stdInInput)
	{
		configuredStdIn = new ByteArrayInputStream(stdInInput.getBytes(StandardCharsets.UTF_8));
	}
}
