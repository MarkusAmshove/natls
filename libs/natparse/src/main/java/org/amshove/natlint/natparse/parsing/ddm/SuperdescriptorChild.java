package org.amshove.natlint.natparse.parsing.ddm;

public class SuperdescriptorChild
{
	private final String name;
	private final int rangeFrom;
	private final int rangeTo;

	public SuperdescriptorChild(String name, int rangeFrom, int rangeTo)
	{
		this.name = name;
		this.rangeFrom = rangeFrom;
		this.rangeTo = rangeTo;
	}

	public String name()
	{
		return name;
	}

	public int rangeFrom()
	{
		return rangeFrom;
	}

	public int rangeTo()
	{
		return rangeTo;
	}
}
