package org.amshove.natlint.natparse.linting;

public class SyntaxToken
{

	private final SyntaxKind kind;
	private final int offset;
	private final int offsetInLine;
	private final int line;
	private final String source;

	public SyntaxKind getKind()
	{
		return kind;
	}

	public int getOffset()
	{
		return offset;
	}

	public int getOffsetInLine()
	{
		return offsetInLine;
	}

	public int getLine()
	{
		return line;
	}

	public String getSource()
	{
		return source;
	}

	public int getLength()
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
			getLength(),
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
