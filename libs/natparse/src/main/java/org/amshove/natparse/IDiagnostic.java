package org.amshove.natparse;

public interface IDiagnostic extends IPosition
{
	String id();

	String message();

	DiagnosticSeverity severity();

	ReadOnlyList<AdditionalDiagnosticInfo> additionalInfo();
}
