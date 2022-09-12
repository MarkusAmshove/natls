package org.amshove.natlint.cli;

import com.google.common.io.CharSink;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import org.amshove.natparse.IDiagnostic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

public class CsvDiagnosticSink implements IDiagnosticSink
{
	protected final CharSink sink;

	public CsvDiagnosticSink(Path filePath)
	{
		this.sink = Files.asCharSink(filePath.toFile(), StandardCharsets.UTF_8, FileWriteMode.APPEND);
		try
		{
			sink.write("FilePath;Id;Severity;Message;Line;OffsetInLine;Length%n".formatted());
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void printDiagnostics(int currentFileCount, Path filePath, List<IDiagnostic> diagnostics)
	{
		for (var diagnostic : diagnostics)
		{
			try
			{
				System.out.print("\r             ");
				System.out.print("\r" + currentFileCount);
				sink.write("%s;%s;%s;%s;%d;%d;%d%n".formatted(filePath, diagnostic.id(), diagnostic.severity(), diagnostic.message(), diagnostic.line(), diagnostic.offsetInLine(), diagnostic.length()));
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
