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

	public static DataType fromString(String type)
	{
		var format = DataFormat.fromSource(type);
		var lengthString = new StringBuilder();
		for (var i = 1; i < type.length(); i++)
		{
			var ch = type.charAt(i);
			if (!Character.isDigit(ch) && ch != '.' && ch != ',')
			{
				break;
			}

			lengthString.append(ch);
		}

		var length = lengthString.length() > 0 ? Double.parseDouble(lengthString.toString().replace(",", ".")) : 1.0;
		return new DataType(format, length);
	}
}
