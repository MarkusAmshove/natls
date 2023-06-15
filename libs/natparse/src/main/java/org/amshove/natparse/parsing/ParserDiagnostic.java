package org.amshove.natparse.parsing;

import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.IPosition;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.ISyntaxNode;

import java.nio.file.Path;

public class ParserDiagnostic implements IDiagnostic
{
	private final String message;
	private final int offset;
	private final int offsetInLine;
	private final int line;
	private final int length;
	private final Path filePath;
	private final String id;
	private final DiagnosticSeverity severity;
	private final ParserError error;
	private final IPosition originalPosition;

	private ParserDiagnostic(String message, int offset, int offsetInLine, int line, int length, Path filePath, ParserError error)
	{
		this(message, offset, offsetInLine, line, length, filePath, null, error, DiagnosticSeverity.ERROR);
	}

	private ParserDiagnostic(String message, int offset, int offsetInLine, int line, int length, Path filePath, ParserError error, DiagnosticSeverity severity)
	{
		this(message, offset, offsetInLine, line, length, filePath, null, error, severity);
	}

	private ParserDiagnostic(String message, int offset, int offsetInLine, int line, int length, Path filePath, IPosition originalPosition, ParserError error, DiagnosticSeverity severity)
	{
		this.message = message;
		this.offset = offset;
		this.offsetInLine = offsetInLine;
		this.line = line;
		this.length = length;
		this.id = error.id();
		this.filePath = filePath;
		this.severity = severity;
		this.error = error;
		this.originalPosition = originalPosition;
	}

	public static ParserDiagnostic create(String message, int offset, int offsetInLine, int line, int length, Path filePath, ParserError error)
	{
		return new ParserDiagnostic(message, offset, offsetInLine, line, length, filePath, error);
	}

	public static ParserDiagnostic create(String message, ISyntaxNode node, ParserError error)
	{
		return create(message, node.diagnosticPosition(), node.position(), error);
	}

	public static ParserDiagnostic create(String message, ISyntaxNode node, ParserError error, DiagnosticSeverity severity)
	{
		return create(message, node.diagnosticPosition(), node.position(), error, severity);
	}

	public static ParserDiagnostic create(String message, SyntaxToken token, ParserError error)
	{
		return create(message, token.diagnosticPosition(), token, error);
	}

	public static ParserDiagnostic create(String message, IPosition diagnosticPosition, IPosition originalPosition, ParserError error, DiagnosticSeverity severity)
	{
		return new ParserDiagnostic(message, diagnosticPosition.offset(), diagnosticPosition.offsetInLine(), diagnosticPosition.line(), diagnosticPosition.length(), diagnosticPosition.filePath(), originalPosition, error, severity);
	}

	public static ParserDiagnostic create(String message, IPosition diagnosticPosition, IPosition originalPosition, ParserError error)
	{
		return new ParserDiagnostic(message, diagnosticPosition.offset(), diagnosticPosition.offsetInLine(), diagnosticPosition.line(), diagnosticPosition.length(), diagnosticPosition.filePath(), originalPosition, error, DiagnosticSeverity.ERROR);
	}

	public static IDiagnostic create(String message, IPosition diagnosticPosition, ParserError error, DiagnosticSeverity severity)
	{
		return new ParserDiagnostic(message, diagnosticPosition.offset(), diagnosticPosition.offsetInLine(), diagnosticPosition.line(), diagnosticPosition.length(), diagnosticPosition.filePath(), error, severity);
	}

	@Override
	public String id()
	{
		return id;
	}

	@Override
	public int offset()
	{
		return offset;
	}

	@Override
	public int offsetInLine()
	{
		return offsetInLine;
	}

	@Override
	public int line()
	{
		return line;
	}

	@Override
	public int length()
	{
		return length;
	}

	@Override
	public Path filePath()
	{
		return filePath;
	}

	@Override
	public String message()
	{
		return message;
	}

	@Override
	public String toString()
	{
		return "ParserDiagnostic{" + id + ":" + message + '}';
	}

	@Override
	public DiagnosticSeverity severity()
	{
		return severity;
	}

	IDiagnostic relocate(IPosition relocatedDiagnosticPosition)
	{
		return new ParserDiagnostic(
			message,
			relocatedDiagnosticPosition.offset(),
			relocatedDiagnosticPosition.offsetInLine(),
			relocatedDiagnosticPosition.line(),
			relocatedDiagnosticPosition.length(),
			relocatedDiagnosticPosition.filePath(),
			error
		);
	}

	@Override
	public IPosition originalPosition()
	{
		return originalPosition != null ? originalPosition : this;
	}

	@Override
	public boolean hasOriginalPosition()
	{
		return originalPosition != null;
	}
}
