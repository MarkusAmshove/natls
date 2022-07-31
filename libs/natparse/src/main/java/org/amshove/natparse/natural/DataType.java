package org.amshove.natparse.natural;

public record DataType(DataFormat format, double length) implements IDataType
{
	public static final int DYNAMIC_LENGTH = Integer.MAX_VALUE;

	public static DataType ofDynamicLength(DataFormat format)
	{
		return new DataType(format, DYNAMIC_LENGTH);
	}

	@Override
	public boolean hasDynamicLength()
	{
		return length == DYNAMIC_LENGTH;
	}
}
