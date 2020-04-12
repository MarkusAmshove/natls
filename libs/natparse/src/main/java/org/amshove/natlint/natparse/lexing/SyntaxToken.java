package org.amshove.natlint.natparse.lexing;

public class SyntaxToken
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

	public String escapedSource()
	{
		return escapeSource();
	}

	public int length()
	{
		return source.length();
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
		return String.format("S[Kind=%s; Source='%s'; Offset=%d; Length=%d; Line=%d; LineOffset=%d]",
			kind,
			escapeSource(),
			offset,
			length(),
			line,
			offsetInLine);
	}

	private String escapeSource()
	{
		if (kind != SyntaxKind.NEW_LINE && kind != SyntaxKind.TAB)
		{
			return source;
		}
		return source.replace("\r", "\\r").replace("\n", "\\n").replace("\t", "\\t");
	}
}
