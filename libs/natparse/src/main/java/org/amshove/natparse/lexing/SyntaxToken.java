package org.amshove.natparse.lexing;

import org.amshove.natparse.IPosition;

import java.nio.file.Path;
import java.util.Objects;

public class SyntaxToken implements IPosition
{

	private final SyntaxKind kind;
	private final int offset;
	private final int offsetInLine;
	private final int line;
	private final String source;
	private final Path filePath;
	private String identifierName = null;

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

	public Path filePath()
	{
		return filePath;
	}

	// TODO: Introduce `LiteralToken`?
	public int intValue()
	{
		if(kind.isSystemVariable())
		{
			// TODO(system-variables): Check actual lengths
			return 8;
		}
		return Integer.parseInt(source());
	}

	public String stringValue()
	{
		return source.substring(1, source.length() - 1);
	}

	/**
	 * Returns the token source as symbol name (all uppercase).
	 */
	public String symbolName()
	{
		if (identifierName != null)
		{
			return identifierName;
		}
		identifierName = source.toUpperCase();
		return identifierName;
	}

	public SyntaxToken(SyntaxKind kind, int offset, int lineOffset, int line, String source, Path filePath)
	{
		this.kind = kind;
		this.offset = offset;
		this.offsetInLine = lineOffset;
		this.line = line;
		this.source = source;
		this.filePath = filePath;
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
