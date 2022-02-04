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

	/**
	 *	Create a diagnostic with a formatted message.<br/>
	 *	The message of the {@link DiagnosticDescription} should have format marks understandable by String.format.
	 */
	public LinterDiagnostic createFormattedDiagnostic(IPosition position, Object... formatArgs)
	{
		return new LinterDiagnostic(id, position, severity, message.formatted(formatArgs));
	}

	public String getId()
	{
		return id;
	}
}
