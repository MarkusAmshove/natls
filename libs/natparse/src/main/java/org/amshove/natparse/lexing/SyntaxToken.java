package org.amshove.natparse.lexing;

import org.amshove.natparse.IPosition;

import java.util.Objects;

public class SyntaxToken implements IPosition
{

	private final SyntaxKind kind;
	private final int offset;
	private final int offsetInLine;
	private final int line;
	private final String source;

	public SyntaxKind kind()
	{
		return kind;
	}

	public int offset()
	{
		return offset;
	}

	public int offsetInLine()
	{
		return offsetInLine;
	}

	public int line()
	{
		return line;
	}

	public String source()
	{
		return source;
	}

	public int length()
	{
		return source.length();
	}

	// TODO: Introduce `LiteralToken`?
	public int intValue()
	{
		return Integer.parseInt(source());
	}

	public SyntaxToken(SyntaxKind kind, int offset, int lineOffset, int line, String source)
	{
		this.kind = kind;
		this.offset = offset;
		this.offsetInLine = lineOffset;
		this.line = line;
		this.source = source;
	}

	@Override
	public String toString()
	{
		return String.format("T[Kind=%s; Source='%s'; Offset=%d; Length=%d; Line=%d; LineOffset=%d]",
			kind,
			source,
			offset,
			length(),
			line,
			offsetInLine);
	}

	public boolean equalsByPosition(SyntaxToken other)
	{
		return other != null && offset == other.offset && line == other.line && offsetInLine == other.offsetInLine;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(kind, offset, offsetInLine, line, source);
	}
}
