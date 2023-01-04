package org.amshove.natlint.cli;

import java.nio.file.Paths;

public enum DiagnosticSinkType
{
	STDOUT,
	NONE,
	CSV,
	CI_CSV;

	public IDiagnosticSink createSink()
	{
		return switch (this)
		{
			case STDOUT -> new AnsiDiagnosticSink();
			case NONE -> new NullDiagnosticSink();
			case CSV -> new CsvDiagnosticSink(Paths.get("diagnostics.csv"));
			case CI_CSV -> new CiCsvDiagnosticSink(Paths.get("diagnostics.csv"));
		};
	}
}
