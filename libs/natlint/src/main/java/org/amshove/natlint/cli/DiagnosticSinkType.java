package org.amshove.natlint.cli;

import org.amshove.natlint.cli.sinks.*;

import java.nio.file.Path;

public enum DiagnosticSinkType
{
	STDOUT,
	NONE,
	CSV,
	SPLIT_CSV;

	public IDiagnosticSink createSink(Path workspace)
	{
		return switch (this)
		{
			case STDOUT -> new AnsiDiagnosticSink();
			case NONE -> new NullDiagnosticSink();
			case CSV -> new CsvDiagnosticSink(workspace.resolve("diagnostics.csv"));
			case SPLIT_CSV -> new SplitCsvDiagnosticSink(workspace);
		};
	}
}
