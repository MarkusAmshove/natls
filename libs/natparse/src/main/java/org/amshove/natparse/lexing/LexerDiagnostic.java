package org.amshove.natparse.lexing;

import org.amshove.natparse.IPosition;

import java.util.Objects;

public class LexerDiagnostic implements IPosition
{
	private final int offset;
	private final int offsetInLine;
	private final int currentLine;
	private final int length;
	private final LexerError error;
	private final String message;

	private LexerDiagnostic(String message, int offset, int offsetInLine, int currentLine, int length, LexerError error)
	{
		this.message = message;
		this.offset = offset;
		this.offsetInLine = offsetInLine;
		this.currentLine = currentLine;
		this.length = length;
		this.error = error;
	}

	static LexerDiagnostic create(String message, int offset, int offsetInLine, int currentLine, int length, LexerError error)
	{
		return new LexerDiagnostic(
			message,
			offset,
			offsetInLine,
			currentLine,
			length,
			error
		);
	}

	static LexerDiagnostic create(int offset, int offsetInLine, int currentLine, int length, LexerError error)
	{
		return new LexerDiagnostic(
			"",
			offset,
			offsetInLine,
			currentLine,
			length,
			error
		);
	}

	public String message()
	{
		return message;
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
	public int currentLine()
	{
		return currentLine;
	}

	@Override
	public int length()
	{
		return length;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		var that = (LexerDiagnostic) o;
		return offset == that.offset && offsetInLine == that.offsetInLine && currentLine == that.currentLine && length == that.length && error == that.error;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(offset, offsetInLine, currentLine, length, error);
	}

	@Override
	public String toString()
	{
		return "LexerDiagnostic{" +
			"offset=" + offset +
			", offsetInLine=" + offsetInLine +
			", currentLine=" + currentLine +
			", length=" + length +
			", error=" + error +
			'}';
	}
}
