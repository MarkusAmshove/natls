package org.amshove.natqube.sensor;

public class CsvDiagnostic
{
	private final String id;
	private final String relativePath;
	private final int line;
	private final int offsetInLine;
	private final int length;
	private final String message;

	public CsvDiagnostic(String id, String relativePath, int line, int offsetInLine, int length, String message)
	{
		this.id = id;
		this.relativePath = relativePath;
		this.line = line;
		this.offsetInLine = offsetInLine;
		this.length = length;
		this.message = message;
	}

	public String getId()
	{
		return id;
	}

	public String getRelativePath()
	{
		return relativePath;
	}

	public int getLine()
	{
		return line + 1;
	}

	public int getOffsetInLine()
	{
		return offsetInLine;
	}

	public int getLength()
	{
		return length;
	}

	public String getMessage()
	{
		return message;
	}

	@Override
	public String toString()
	{
		return "CsvDiagnostic{" +
			"id='" + id + '\'' +
			", relativePath=" + relativePath +
			", line=" + line +
			", offsetInLine=" + offsetInLine +
			", length=" + length +
			", message='" + message + '\'' +
			'}';
	}
}
