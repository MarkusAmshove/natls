package org.amshove.natlint.api;

import org.amshove.natparse.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LinterDiagnostic implements IDiagnostic
{
	private final String id;
	private final IPosition position;
	private final DiagnosticSeverity severity;
	private final String message;
	private final IPosition originalPosition;
	private final List<AdditionalDiagnosticInfo> additionalInfos;

	public LinterDiagnostic(String id, IPosition position, DiagnosticSeverity severity, String message)
	{
		this(id, position, null, severity, message);
	}

	public LinterDiagnostic(String id, IPosition position, IPosition originalPosition, DiagnosticSeverity severity, String message)
	{
		this(id, position, originalPosition, severity, message, new ArrayList<>());
		if (originalPosition != null && !originalPosition.isSamePositionAs(position))
		{
			additionalInfos.add(new AdditionalDiagnosticInfo("Occurred here", originalPosition));
		}
	}

	public LinterDiagnostic(String id, IPosition position, IPosition originalPosition, DiagnosticSeverity severity, String message, List<AdditionalDiagnosticInfo> additionalInfos)
	{
		this.id = id;
		this.position = position;
		this.severity = severity;
		this.message = message;
		this.originalPosition = originalPosition;
		this.additionalInfos = additionalInfos;
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
	public ReadOnlyList<AdditionalDiagnosticInfo> additionalInfo()
	{
		return ReadOnlyList.from(additionalInfos);
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

	public LinterDiagnostic withSeverity(DiagnosticSeverity newSeverity)
	{
		return new LinterDiagnostic(
			id,
			position,
			originalPosition,
			newSeverity,
			message,
			additionalInfos
		);
	}

	public void addAdditionalInfo(AdditionalDiagnosticInfo info)
	{
		additionalInfos.add(info);
	}
}
