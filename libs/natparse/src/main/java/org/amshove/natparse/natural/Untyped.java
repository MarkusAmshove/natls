package org.amshove.natparse.natural;

public record Untyped() implements IDataType
{
	@Override
	public DataFormat format()
	{
		return DataFormat.NONE;
	}

	@Override
	public double length()
	{
		return 0;
	}

	@Override
	public boolean hasDynamicLength()
	{
		return false;
	}
}
