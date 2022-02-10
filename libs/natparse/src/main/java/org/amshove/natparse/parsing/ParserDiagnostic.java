package org.amshove.natparse.parsing;

import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.IPosition;
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

	private ParserDiagnostic(String message, int offset, int offsetInLine, int line, int length, Path filePath, ParserError error)
	{
		this.message = message;
		this.offset = offset;
		this.offsetInLine = offsetInLine;
		this.line = line;
		this.length = length;
		this.id = error.id();
		this.filePath = filePath;
		severity = DiagnosticSeverity.ERROR;
		this.error = error;
	}

	public static ParserDiagnostic create(String message, int offset, int offsetInLine, int line, int length, Path filePath, ParserError error)
	{
		return new ParserDiagnostic(message, offset, offsetInLine, line, length, filePath, error);
	}

	public static ParserDiagnostic create(String message, ISyntaxNode node, ParserError error)
	{
		return create(message, node.position(), error);
	}

	public static ParserDiagnostic create(String message, IPosition position, ParserError error)
	{
		return new ParserDiagnostic(message, position.offset(), position.offsetInLine(), position.line(), position.length(), position.filePath(), error);
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
}
