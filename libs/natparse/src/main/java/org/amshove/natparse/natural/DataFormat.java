package org.amshove.natparse.natural;

import org.amshove.natparse.NaturalParseException;

public enum DataFormat
{
	ALPHANUMERIC,
	BINARY,
	CONTROL,
	DATE,
	FLOAT,
	INTEGER,
	LOGIC,
	NUMERIC,
	PACKED,
	TIME,
	UNICODE,
	NONE;

	/**
	 * Constructs the DataFormat from source.
	 *
	 * @param source - 1 character long data format (e.g. A for Alphanumeric)
	 * @return the typed DataFormat
	 */
	public static DataFormat fromSource(String source)
	{
		return switch (source)
			{
				case "A" -> ALPHANUMERIC;
				case "B" -> BINARY;
				case "C" -> CONTROL;
				case "D" -> DATE;
				case "F" -> FLOAT;
				case "I" -> INTEGER;
				case "L" -> LOGIC;
				case "N" -> NUMERIC;
				case "P" -> PACKED;
				case "T" -> TIME;
				case "U" -> UNICODE;
				case " " -> NONE;
				default -> throw new NaturalParseException(String.format("Can't determine DataFormat from format \"%s\"", source));
			};
	}
}
