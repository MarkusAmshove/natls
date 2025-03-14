package org.amshove.natqube.sensor;

import java.net.URI;

public class CsvDiagnostic
{
	private final String id;
	private final URI fileUri;
	private final int line;
	private final int offsetInLine;
	private final int length;
	private final String message;

	public CsvDiagnostic(String id, URI fileUri, int line, int offsetInLine, int length, String message)
	{
		this.id = id;
		this.fileUri = fileUri;
		this.line = line;
		this.offsetInLine = offsetInLine;
		this.length = length;
		this.message = message;
	}

	public String getId()
	{
		return id;
	}

	public URI getFileUri()
	{
		return fileUri;
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
			", fileUri=" + fileUri +
			", line=" + line +
			", offsetInLine=" + offsetInLine +
			", length=" + length +
			", message='" + message + '\'' +
			'}';
	}
}
