package org.amshove.natparse.natural;

public interface IDataType
{
	int ONE_GIGABYTE = 1073741824;

	DataFormat format();

	/**
	 * Returns the `literal` length as defined in the source, e.g. 2 for A2.
	 */
	double length();

	boolean hasDynamicLength();

	default boolean fitsInto(IDataType target)
	{
		var bytesFit = this.byteSize() <= target.byteSize();
		var targetFormat = target.format();
		var ourFormat = format();
		var formatIsCompatible = ourFormat == targetFormat || switch (ourFormat)
		{
			case PACKED, FLOAT, INTEGER, NUMERIC -> targetFormat == DataFormat.ALPHANUMERIC || targetFormat == DataFormat.UNICODE;
			default -> false; // we don't know whats implicitly compatible yet
		};

		return bytesFit && formatIsCompatible;
	}

	default String toShortString()
	{
		var details = "";

		details += "(%s".formatted(format().identifier());
		if (length() > 0.0)
		{
			details += "%s".formatted(DataFormat.formatLength(length()));
		}
		details += ")";

		if (hasDynamicLength())
		{
			details += " DYNAMIC";
		}

		return details;
	}

	/**
	 * Returns the actual size in bytes.
	 */
	default int byteSize()
	{
		return switch (format())
		{
			case ALPHANUMERIC, BINARY -> hasDynamicLength()
				? ONE_GIGABYTE // max internal length in bytes
				: (int) length();
			case FLOAT, INTEGER -> (int) length();
			case CONTROL -> 2;
			case DATE -> 4;
			case LOGIC -> 1;
			case NUMERIC -> calculateNumericSize();
			case PACKED -> calculatePackedSize();
			case TIME -> 7;
			case UNICODE -> hasDynamicLength()
				? ONE_GIGABYTE // max internal length in bytes
				: Math.max((int) length(), 2);
			case NONE -> 0;
		};
	}

	/**
	 * Returns the sum of all digits. For example 9 for N7,2
	 */
	default int sumOfDigits()
	{
		return calculateNumericSize();
	}

	private int calculateNumericSize()
	{
		var digitsBeforeDecimalPoint = (int) length();
		var digitsAfterDecimalPoint = calculateDigitsAfterDecimalPoint();

		return Math.max(1, digitsBeforeDecimalPoint + digitsAfterDecimalPoint);
	}

	private int calculatePackedSize()
	{
		return Math.max(1, (int) (Math.round((calculateNumericSize() + 1) / 2.0)));
	}

	private int calculateDigitsAfterDecimalPoint()
	{
		return Integer.parseInt((Double.toString(length()).split("\\.")[1]));
	}
}
