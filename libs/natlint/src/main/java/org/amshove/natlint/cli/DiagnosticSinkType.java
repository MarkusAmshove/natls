package org.amshove.natlint.cli;

public enum DiagnosticSinkType
{
	STDOUT,
	NONE;

	public IDiagnosticSink createSink()
	{
		return switch (this)
			{
				case STDOUT -> new AnsiDiagnosticSink();
				case NONE -> new NullDiagnosticSink();
			};
	}
}
