package org.amshove.natlint.cli.sinks;

import org.amshove.natparse.IDiagnostic;

import java.nio.file.Path;
import java.util.List;

public interface IDiagnosticSink
{
	void printDiagnostics(int currentFileCount, Path filePath, List<IDiagnostic> diagnostics);
}
