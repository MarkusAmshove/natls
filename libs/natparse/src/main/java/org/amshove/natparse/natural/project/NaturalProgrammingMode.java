package org.amshove.natparse.natural.project;

public enum NaturalProgrammingMode
{
	STRUCTURED("S"),
	REPORTING("R"),
	UNKNOWN("?");

	public static final NaturalProgrammingMode[] VALUES = NaturalProgrammingMode.values();

	public static NaturalProgrammingMode fromString(String mode)
	{
		return switch (mode)
		{
			case "S" -> STRUCTURED;
			case "R" -> REPORTING;
			default -> UNKNOWN;
		};
	}

	private final String mode;

	NaturalProgrammingMode(String mode)
	{
		this.mode = mode;
	}

	public String getMode()
	{
		return mode;
	}
}
