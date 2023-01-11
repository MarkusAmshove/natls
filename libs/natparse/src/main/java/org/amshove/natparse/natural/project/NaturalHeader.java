package org.amshove.natparse.natural.project;

public class NaturalHeader
{
	private NaturalProgrammingMode programmingMode;
	private int lineIncrement;

	public NaturalHeader(NaturalProgrammingMode mode, int lineIncrement)
	{
		this.programmingMode = mode;
		this.lineIncrement = lineIncrement;
	}

	public NaturalProgrammingMode getProgrammingMode()
	{
		return programmingMode;
	}

	public int getLineIncrement()
	{
		return lineIncrement;
	}

	public boolean isStructuredMode()
	{
		return programmingMode == NaturalProgrammingMode.STRUCTURED;
	}

	public boolean isReportingMode()
	{
		return programmingMode == NaturalProgrammingMode.REPORTING;
	}
}
