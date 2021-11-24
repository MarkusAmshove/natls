package org.amshove.natparse;

public interface IDiagnostic extends IPosition
{
	String id();
	String message();
	DiagnosticSeverity severity();

	default String toVerboseString()
	{
		return "Diagnostic{line=%d, id='%s', severity=%s}".formatted(line(), id(), severity());
	}
}
