package org.amshove.natlint.natparse.parsing.ddm;

class DdmMetadata
{
	private String name;
	private String databaseNumber;
	private String fileNumber;
	private String defaultSequence;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDatabaseNumber()
	{
		return databaseNumber;
	}

	public void setDatabaseNumber(String databaseNumber)
	{
		this.databaseNumber = databaseNumber;
	}

	public String getFileNumber()
	{
		return fileNumber;
	}

	public void setFileNumber(String fileNumber)
	{
		this.fileNumber = fileNumber;
	}

	public String getDefaultSequence()
	{
		return defaultSequence;
	}

	public void setDefaultSequence(String defaultSequence)
	{
		this.defaultSequence = defaultSequence;
	}
}
