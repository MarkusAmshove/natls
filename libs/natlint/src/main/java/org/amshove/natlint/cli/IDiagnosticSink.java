package org.amshove.natlint.cli;

import org.amshove.natparse.IDiagnostic;

import java.nio.file.Path;
import java.util.List;

public interface IDiagnosticSink
{
	void printDiagnostics(Path filePath, List<IDiagnostic> diagnostics);
}
