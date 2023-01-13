package org.amshove.natlint.api;

import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.IPosition;
import org.checkerframework.dataflow.qual.Pure;

import java.nio.file.Path;

public class LinterDiagnostic implements IDiagnostic
{
	private final String id;
	private final IPosition position;
	private final DiagnosticSeverity severity;
	private final String message;
	private final IPosition originalPosition;

	public LinterDiagnostic(String id, IPosition position, DiagnosticSeverity severity, String message)
	{
		this(id, position, null, severity, message);
	}

	public LinterDiagnostic(String id, IPosition position, IPosition originalPosition, DiagnosticSeverity severity, String message)
	{
		this.id = id;
		this.position = position;
		this.severity = severity;
		this.message = message;
		this.originalPosition = originalPosition;
	}

	@Override
	public String id()
	{
		return id;
	}

	@Override
	public String message()
	{
		return message;
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
	public int offset()
	{
		return position.offset();
	}

	@Override
	public int offsetInLine()
	{
		return position.offsetInLine();
	}

	@Override
	public int line()
	{
		return position.line();
	}

	@Override
	public int length()
	{
		return position.length();
	}

	@Override
	public Path filePath()
	{
		return position.filePath();
	}

	@Override
	public String toString()
	{
		return "LinterDiagnostic{" +
			"id='" + id + '\'' +
			", severity=" + severity +
			", message='" + message + '\'' +
			", position=" + position +
			'}';
	}

	@Override
	public boolean hasOriginalPosition()
	{
		return originalPosition != null;
	}

	@Pure
	public LinterDiagnostic withSeverity(DiagnosticSeverity newSeverity)
	{
		return new LinterDiagnostic(
			id,
			position,
			originalPosition,
			newSeverity,
			message
		);
	}
}
