package org.amshove.natlint.api;

import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.IPosition;

import java.nio.file.Path;

public class LinterDiagnostic implements IDiagnostic
{
	private final String id;
	private final IPosition position;
	private final DiagnosticSeverity severity;
	private final String message;

	public LinterDiagnostic(String id, IPosition position, DiagnosticSeverity severity, String message)
	{
		this.id = id;
		this.position = position;
		this.severity = severity;
		this.message = message;
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

	@Override public String toString()
	{
		return "LinterDiagnostic{" +
			"id='" + id + '\'' +
			", severity=" + severity +
			", message='" + message + '\'' +
			", position=" + position +
			'}';
	}
}
