package org.amshove.natlint.api;

import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.IPosition;

public class DiagnosticDescription
{
	private final String id;
	private final String message;
	private final DiagnosticSeverity severity;

	private DiagnosticDescription(String id, String message, DiagnosticSeverity severity)
	{
		this.id = id;
		this.message = message;
		this.severity = severity;
	}

	public static DiagnosticDescription create(String id, String message, DiagnosticSeverity severity)
	{
		return new DiagnosticDescription(id, message, severity);
	}

	public LinterDiagnostic createDiagnostic(IPosition position)
	{
		return new LinterDiagnostic(id, position, severity, message);
	}

	public String getId()
	{
		return id;
	}
}
