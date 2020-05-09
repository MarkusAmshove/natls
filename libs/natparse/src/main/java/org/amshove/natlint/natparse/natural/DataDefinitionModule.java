package org.amshove.natlint.natparse.natural;

public class DataDefinitionModule
{
	private final String databaseNumber;
	private final String fileNumber;
	private final String ddmName;
	private final String defaultSequence;

	public DataDefinitionModule(String databaseNumber, String fileNumber, String ddmName, String defaultSequence)
	{
		this.databaseNumber = databaseNumber;
		this.fileNumber = fileNumber;
		this.ddmName = ddmName;
		this.defaultSequence = defaultSequence;
	}

	public String name()
	{
		return ddmName;
	}

	public String fileNumber()
	{
		return fileNumber;
	}

	public String databaseNumber()
	{
		return databaseNumber;
	}

	public String defaultSequence()
	{
		return defaultSequence;
	}
}
