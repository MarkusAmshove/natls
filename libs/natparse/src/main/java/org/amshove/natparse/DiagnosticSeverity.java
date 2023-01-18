package org.amshove.natparse;

public enum DiagnosticSeverity
{
	INFO(0),
	WARNING(1),
	ERROR(2);

	private final int weight;

	DiagnosticSeverity(int weight)
	{
		this.weight = weight;
	}

	public boolean isWorseOrEqualTo(DiagnosticSeverity other)
	{
		return this.weight >= other.weight;
	}

	public static DiagnosticSeverity fromString(String sev)
	{
		return switch (sev.toUpperCase())
		{
			case "WARN", "WARNING" -> WARNING;
			case "INFO" -> INFO;
			case "ERROR" -> ERROR;
			default -> throw new IllegalArgumentException("Invalid severity: " + sev);
		};
	}
}
