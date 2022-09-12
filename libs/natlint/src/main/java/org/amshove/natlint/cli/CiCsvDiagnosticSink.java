package org.amshove.natlint.cli;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.amshove.natparse.IDiagnostic;

public class CiCsvDiagnosticSink extends CsvDiagnosticSink
{
	public CiCsvDiagnosticSink(Path filePath)
	{
		super(filePath);
	}

	@Override
	public void printDiagnostics(int currentFileCount, Path filePath, List<IDiagnostic> diagnostics)
	{
		for (var diagnostic : diagnostics)
		{
			try
			{
				sink.write("%s;%s;%s;%s;%d;%d;%d%n".formatted(filePath, diagnostic.id(), diagnostic.severity(), diagnostic.message(), diagnostic.line(), diagnostic.offsetInLine(), diagnostic.length()));
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
