package org.amshove.natlint.api;

import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.IPosition;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.ISyntaxNode;

public class DiagnosticDescription
{
	private final String id;
	private final String message;
	private final DiagnosticSeverity severity;

	public String getMessage()
	{
		return message;
	}

	public DiagnosticSeverity getSeverity()
	{
		return severity;
	}

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

	public LinterDiagnostic createDiagnostic(ISyntaxNode node)
	{
		return createFormattedDiagnostic(node.diagnosticPosition(), node);
	}

	/**
	 *	Create a diagnostic with a formatted message.<br/>
	 *	The message of the {@link DiagnosticDescription} should have format marks understandable by String.format.
	 */
	public LinterDiagnostic createFormattedDiagnostic(IPosition position, Object... formatArgs)
	{
		return new LinterDiagnostic(id, position, severity, message.formatted(formatArgs));
	}

	/**
	 *	Create a diagnostic with a formatted message.<br/>
	 *	Supports passing the original location of the diagnostic. See {@link IDiagnostic#originalPosition()} <br/>
	 *	The message of the {@link DiagnosticDescription} should have format marks understandable by String.format. <br/>
	 */
	public LinterDiagnostic createFormattedDiagnostic(IPosition position, IPosition originalPosition, Object... formatArgs)
	{
		return new LinterDiagnostic(id, position, originalPosition, severity, message.formatted(formatArgs));
	}

	/**
	 *	Create a diagnostic with a formatted message.<br/>
	 *	Supports passing the original location of the diagnostic. See {@link IDiagnostic#originalPosition()} <br/>
	 *	The message of the {@link DiagnosticDescription} should have format marks understandable by String.format. <br/>
	 */
	public LinterDiagnostic createFormattedDiagnostic(SyntaxToken token, Object... formatArgs)
	{
		return createFormattedDiagnostic(token.diagnosticPosition(), token, formatArgs);
	}

	public String getId()
	{
		return id;
	}
}
