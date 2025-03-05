package org.amshove.natlint.cli.sinks;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import com.google.common.io.CharSink;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import org.amshove.natparse.IDiagnostic;

public class SonarCsvDiagnosticSink implements IDiagnosticSink
{
	private CharSink currentSink;
	private int currentFileCount = 0;
	private int currentDiagnosticCount = 0;

	public SonarCsvDiagnosticSink()
	{}

	@Override
	public synchronized void printDiagnostics(int currentFileCount, Path filePath, List<IDiagnostic> diagnostics)
	{
		startNewFileIfNecessary();
		for (var diagnostic : diagnostics)
		{
			currentDiagnosticCount++;
			try
			{
				System.out.print("\r             ");
				System.out.print("\r" + currentFileCount);
				currentSink.write("%s;%s;%s;%s;%d;%d;%d%n".formatted(filePath, diagnostic.id(), diagnostic.severity(), diagnostic.message(), diagnostic.line(), diagnostic.offsetInLine(), diagnostic.length()));
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	private void startNewFileIfNecessary()
	{
		if (currentDiagnosticCount > 40_000)
		{
			currentFileCount++;
			currentDiagnosticCount = 0;
			currentSink = null;
		}

		if (currentSink == null)
		{
			try
			{
				currentSink = Files.asCharSink(Path.of("natqube-diagnostics-%d.csv".formatted(currentFileCount)).toFile(), StandardCharsets.UTF_8, FileWriteMode.APPEND);
				currentSink.write("file;ruleId;severity;message;line;offset;length%n");
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
