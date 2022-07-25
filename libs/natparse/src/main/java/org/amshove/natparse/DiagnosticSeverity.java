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
}
