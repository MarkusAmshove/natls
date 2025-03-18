package org.amshove.natlint.cli.sinks;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import com.google.common.io.CharSink;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import org.amshove.natparse.IDiagnostic;

public class SplitCsvDiagnosticSink implements IDiagnosticSink
{
	private final Path projectRootDirectoryPath;
	private final Path directoryForCsvFiles;
	private CharSink currentSink;
	private int currentFileCount = 1;
	private int currentDiagnosticCount = 0;

	public SplitCsvDiagnosticSink(Path projectRootDirectoryPath)
	{
		this.projectRootDirectoryPath = projectRootDirectoryPath;
		this.directoryForCsvFiles = projectRootDirectoryPath.resolve("natlint");
	}

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
				currentSink.write("%s;%s;%s;%s;%d;%d;%d%n".formatted(projectRootDirectoryPath.relativize(filePath), diagnostic.id(), diagnostic.severity(), diagnostic.message(), diagnostic.line(), diagnostic.offsetInLine(), diagnostic.length()));
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
				var diagnosticFilePath = directoryForCsvFiles.resolve("diagnostics-%d.csv".formatted(currentFileCount));

				var diagnosticFile = diagnosticFilePath.toFile();
				Files.createParentDirs(diagnosticFile);

				currentSink = Files.asCharSink(diagnosticFile, StandardCharsets.UTF_8, FileWriteMode.APPEND);
				currentSink.write("file;ruleId;severity;message;line;offset;length%n".formatted());
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
