package org.amshove.natlint.cli;

import org.amshove.natparse.IDiagnostic;

import java.nio.file.Path;
import java.util.List;

public class NullDiagnosticSink implements IDiagnosticSink
{
	@Override
	public void printDiagnostics(Path filePath, List<IDiagnostic> diagnostics)
	{

	}
}
