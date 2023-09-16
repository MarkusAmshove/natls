package org.amshove.natlint.cli;

import org.amshove.natlint.api.LinterDiagnostic;
import org.amshove.natlint.cli.sinks.FileStatusSink;
import org.amshove.natlint.cli.sinks.FileStatusSink.MessageType;
import org.amshove.natparse.DiagnosticSeverity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class FileStatusSinkShould
{
	@Test
	void printInfoToFile(@TempDir Path tempDirectory) throws IOException
	{
		var sinkFile = tempDirectory.resolve("result.csv");
		var natFile = tempDirectory.resolve("test.NSP");

		var sut = new FileStatusSink(sinkFile);
		sut.printStatus(natFile, MessageType.SUCCESS);

		assertThat(sut.isEnabled()).isTrue();
		assertThat(Files.lines(sinkFile).count()).isEqualTo(2);
	}

	@Test
	void printErrorToFile(@TempDir Path tempDirectory) throws IOException
	{
		var sinkFile = tempDirectory.resolve("result.csv");
		var natFile = tempDirectory.resolve("test.NSP");

		var errorMessage = "This went wrong";
		var ex = new Exception(errorMessage);

		var sut = new FileStatusSink(sinkFile);
		sut.printError(natFile, MessageType.LINT_EXCEPTION, ex);

		assertThat(sut.isEnabled()).isTrue();
		assertThat(Files.lines(sinkFile).count()).isEqualTo(2);
		assertThat(Files.lines(sinkFile).toList().get(1)).contains(errorMessage);
	}

	@Test
	void printDiagnosticsToFile(@TempDir Path tempDirectory) throws IOException
	{
		var sinkFile = tempDirectory.resolve("result.csv");
		var natFile = tempDirectory.resolve("test.NSP");

		var diagnostics = new ArrayList<LinterDiagnostic>();
		diagnostics.add(new LinterDiagnostic("idA", null, DiagnosticSeverity.ERROR, "code smell"));
		diagnostics.add(new LinterDiagnostic("idB", null, DiagnosticSeverity.ERROR, "code smell"));
		diagnostics.add(new LinterDiagnostic("idC", null, DiagnosticSeverity.ERROR, "code smell"));

		var sut = new FileStatusSink(sinkFile);
		sut.printDiagnostics(natFile, MessageType.LINT_FAILED, diagnostics);

		assertThat(sut.isEnabled()).isTrue();
		assertThat(Files.lines(sinkFile).count()).isEqualTo(2);
		assertThat(Files.lines(sinkFile).toList().get(1)).contains("3");

	}

	@Test
	void printToDummy(@TempDir Path tempDirectory) throws IOException
	{
		var natFile = tempDirectory.resolve("test.NSP");

		var ex = new Exception("This went wrong");
		var diagnostics = new ArrayList<LinterDiagnostic>();
		diagnostics.add(new LinterDiagnostic("id", null, DiagnosticSeverity.ERROR, "code smell"));

		var sut = FileStatusSink.dummy();
		sut.printStatus(natFile, MessageType.SUCCESS);
		sut.printError(natFile, MessageType.LINT_EXCEPTION, ex);
		sut.printDiagnostics(natFile, MessageType.LINT_FAILED, diagnostics);

		assertThat(sut.isEnabled()).isFalse();
	}

	@Test
	void printOnlyOnceToFile(@TempDir Path tempDirectory) throws IOException
	{
		var sinkFile = tempDirectory.resolve("result.csv");
		var natFile = tempDirectory.resolve("test.NSP");

		var sut = new FileStatusSink(sinkFile);
		sut.printStatus(natFile, MessageType.FILE_EXCLUDED);
		sut.printStatus(natFile, MessageType.REPORTING_TYPE);
		sut.printStatus(natFile, MessageType.SUCCESS);

		assertThat(sut.isEnabled()).isTrue();
		assertThat(Files.lines(sinkFile).count()).isEqualTo(2);
	}

	@Test
	void clearFileBeforePrintingToFile(@TempDir Path tempDirectory) throws IOException
	{
		var sinkFile = tempDirectory.resolve("result.csv");
		var natFile = tempDirectory.resolve("test.NSP");

		Files.writeString(sinkFile, """
			One Line
			Two Lines
			Three Lines
			""");

		var sut = new FileStatusSink(sinkFile);
		sut.printStatus(natFile, MessageType.SUCCESS);

		assertThat(sut.isEnabled()).isTrue();
		assertThat(Files.lines(sinkFile).count()).isEqualTo(2);
	}
}
