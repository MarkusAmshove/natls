package org.amshove.natparse.natural;

import static org.amshove.natparse.natural.DataFormat.*;

public interface IDataType
{
	int ONE_GIGABYTE = 1073741824;
	IDataType UNTYPED = new Untyped();

	DataFormat format();

	/**
	 * Returns the `literal` length as defined in the source, e.g. 2 for A2.
	 */
	double length();

	boolean hasDynamicLength();

	default boolean isNumericFamily()
	{
		return format() == NUMERIC || format() == PACKED || format() == FLOAT || format() == INTEGER;
	}

	default boolean isAlphaNumericFamily()
	{
		return format() == ALPHANUMERIC || format() == UNICODE || format() == BINARY;
	}

	/**
	 * Determines if this type fits into the given type. Implicit conversion is taken into account.<br/>
	 * <strong>This does not compare by byte size</strong>
	 */
	default boolean fitsInto(IDataType target)
	{
		var ourLength = this.hasDynamicLength() ? ONE_GIGABYTE : byteSize();
		var theirLength = target.hasDynamicLength() ? ONE_GIGABYTE : target.byteSize();
		var lengthFits = ourLength <= theirLength;
		var formatIsCompatible = hasCompatibleFormat(target);

		return lengthFits && formatIsCompatible;
	}

	/**
	 * Determines if both types have the same family, e.g. N, I, P are all numeric.
	 */
	default boolean hasSameFamily(IDataType target)
	{
		var targetFormat = target.format();
		return format() == targetFormat || switch (format())
		{
			case PACKED, FLOAT, INTEGER, NUMERIC, TIME -> targetFormat == PACKED
				|| targetFormat == FLOAT
				|| targetFormat == INTEGER
				|| targetFormat == NUMERIC
				|| targetFormat == TIME;
			case ALPHANUMERIC, UNICODE -> targetFormat == ALPHANUMERIC
				|| targetFormat == UNICODE
				|| targetFormat == BINARY;
			default -> false;
		};
	}

	/**
	 * Takes implicit conversion into account, e.g. N -> A
	 */
	default boolean hasCompatibleFormat(IDataType target)
	{
		var targetFormat = target.format();
		return hasSameFamily(target) || switch (format())
		{
			case PACKED, FLOAT, INTEGER, NUMERIC -> targetFormat == ALPHANUMERIC
				|| targetFormat == UNICODE
				|| isShortBinary(target);
			case TIME, DATE -> targetFormat == NUMERIC
				|| targetFormat == PACKED
				|| targetFormat == ALPHANUMERIC
				|| targetFormat == UNICODE
				|| isShortBinary(target)
				|| targetFormat == INTEGER // this one can fail, but not for early times
				|| targetFormat == DATE
				|| targetFormat == TIME
				|| targetFormat == FLOAT;
			case LOGIC -> targetFormat == ALPHANUMERIC
				|| targetFormat == UNICODE;
			case BINARY -> binaryCompatibility(target);
			default -> false; // we don't know whats implicitly compatible yet
		};
	}

	default String toShortString()
	{
		var details = "";

		details += "(%s".formatted(format().identifier());
		if (length() > 0.0 && !hasDynamicLength() && format().canHaveUserDefinedLength())
		{
			details += "%s".formatted(formatLength(length()));
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

	default int calculateNumericSize()
	{
		var digitsBeforeDecimalPoint = (int) length();
		var digitsAfterDecimalPoint = calculateDigitsAfterDecimalPoint();

		return Math.max(1, digitsBeforeDecimalPoint + digitsAfterDecimalPoint);
	}

	default int calculatePackedSize()
	{
		return Math.max(1, (int) (Math.round((calculateNumericSize() + 1) / 2.0)));
	}

	private int calculateDigitsAfterDecimalPoint()
	{
		return Integer.parseInt((Double.toString(length()).split("\\.")[1]));
	}

	private boolean isShortBinary()
	{
		return format() == BINARY && length() < 5;
	}

	private boolean isShortBinary(IDataType target)
	{
		return target.format() == BINARY && target.length() < 5;
	}

	private boolean isLongBinary()
	{
		return format() == BINARY && length() > 4;
	}

	private boolean binaryCompatibility(IDataType target)
	{
		var targetFormat = target.format();
		return (isLongBinary() && switch (targetFormat)
		{
			case ALPHANUMERIC, UNICODE -> true;
			default -> false;
		}) ||
			(isShortBinary() && switch (targetFormat)
			{
			case NUMERIC, PACKED, ALPHANUMERIC, UNICODE, INTEGER, TIME, FLOAT -> true;
			default -> false;
			});
	}
}
