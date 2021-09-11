package org.amshove.natlint.natparse.parsing.ddm;

class DdmMetadata
{
	private String name;
	private String databaseNumber;
	private String fileNumber;
	private String defaultSequence;

	public String name()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String databaseNumber()
	{
		return databaseNumber;
	}

	public void setDatabaseNumber(String databaseNumber)
	{
		this.databaseNumber = databaseNumber;
	}

	public String fileNumber()
	{
		return fileNumber;
	}

	public void setFileNumber(String fileNumber)
	{
		this.fileNumber = fileNumber;
	}

	public String defaultSequence()
	{
		return defaultSequence;
	}

	public void setDefaultSequence(String defaultSequence)
	{
		this.defaultSequence = defaultSequence;
	}
}
