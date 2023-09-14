package org.amshove.natparse.lexing;

import org.amshove.natparse.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class LexerDiagnostic implements IDiagnostic
{
	private final String id;
	private final int offset;
	private final int offsetInLine;
	private final int line;
	private final int length;
	private final Path filePath;
	private final LexerError error;
	private final String message;
	private final DiagnosticSeverity severity;
	private final IPosition originalPosition;
	private final List<AdditionalDiagnosticInfo> additionalInfos = new ArrayList<>();

	private LexerDiagnostic(String message, int offset, int offsetInLine, int currentLine, int length, Path filePath, LexerError error)
	{
		this(message, offset, offsetInLine, currentLine, length, filePath, null, error);
	}

	private LexerDiagnostic(String message, int offset, int offsetInLine, int currentLine, int length, Path filePath, IPosition originalPosition, LexerError error)
	{
		this.message = message;
		this.offset = offset;
		this.offsetInLine = offsetInLine;
		this.line = currentLine;
		this.length = length;
		this.error = error;
		this.id = error.id();
		this.filePath = filePath;
		this.originalPosition = originalPosition;
		severity = DiagnosticSeverity.ERROR;
	}

	static LexerDiagnostic create(String message, int offset, int offsetInLine, int currentLine, int length, Path filePath, LexerError error)
	{
		return new LexerDiagnostic(
			message,
			offset,
			offsetInLine,
			currentLine,
			length,
			filePath,
			error
		);
	}

	static LexerDiagnostic create(int offset, int offsetInLine, int currentLine, int length, Path filePath, LexerError error)
	{
		return new LexerDiagnostic(
			"",
			offset,
			offsetInLine,
			currentLine,
			length,
			filePath,
			error
		);
	}

	static LexerDiagnostic create(String message, int offset, int offsetInLine, int currentLine, int length, Path filePath, IPosition originalPosition, LexerError error)
	{
		return new LexerDiagnostic(
			message,
			offset,
			offsetInLine,
			currentLine,
			length,
			filePath,
			originalPosition,
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

	public String id()
	{
		return id;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		var that = (LexerDiagnostic) o;
		return offset == that.offset && offsetInLine == that.offsetInLine && line == that.line && length == that.length && error == that.error;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(offset, offsetInLine, line, length, error);
	}

	@Override
	public String toString()
	{
		return "LexerDiagnostic{" + id + ":" + message + '}';
	}

	@Override
	public DiagnosticSeverity severity()
	{
		return severity;
	}

	@Override
	public IPosition originalPosition()
	{
		return originalPosition != null ? originalPosition : this;
	}

	@Override
	public ReadOnlyList<AdditionalDiagnosticInfo> additionalInfo()
	{
		return ReadOnlyList.from(additionalInfos);
	}

	@Override
	public boolean hasOriginalPosition()
	{
		return originalPosition != null;
	}

	void addAdditionalInfo(AdditionalDiagnosticInfo info)
	{
		additionalInfos.add(info);
	}
}
