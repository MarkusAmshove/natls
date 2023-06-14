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

	/**
	 * Determines if this type fits into the given type. Implicit conversion is taken into account.</br>
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
	default boolean hasSameFamily(IDataType other)
	{
		var targetFormat = other.format();
		return format() == targetFormat || switch (format())
		{
			case PACKED, FLOAT, INTEGER, NUMERIC -> targetFormat == PACKED
				|| targetFormat == FLOAT
				|| targetFormat == INTEGER
				|| targetFormat == NUMERIC
				|| targetFormat == BINARY;
			case ALPHANUMERIC, UNICODE, BINARY -> targetFormat == ALPHANUMERIC
				|| targetFormat == UNICODE
				|| targetFormat == BINARY;
			default -> false;
		};
	}

	/**
	 * Takes implicit conversion into account, e.g. N -> A
	 */
	default boolean hasCompatibleFormat(IDataType other)
	{
		var targetFormat = other.format();
		return hasSameFamily(other) || switch (format())
		{
			case PACKED, FLOAT, INTEGER, NUMERIC -> targetFormat == ALPHANUMERIC
				|| targetFormat == UNICODE
				|| targetFormat == BINARY;
			default -> false; // we don't know whats implicitly compatible yet
		};
	}

	default String toShortString()
	{
		var details = "";

		details += "(%s".formatted(format().identifier());
		if (length() > 0.0)
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
}
