package org.amshove.natlint.cli.sinks;

import org.amshove.natparse.IDiagnostic;

import java.nio.file.Path;
import java.util.List;

public class NullDiagnosticSink implements IDiagnosticSink
{
	@Override
	public void printDiagnostics(int currentFileCount, Path filePath, List<IDiagnostic> diagnostics)
	{
		// this sink doesn't do anything
	}
}
