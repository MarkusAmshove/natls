package org.amshove.natlint.natparse.natural;

import org.amshove.natlint.natparse.NaturalParseException;

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
	UNICODE;

	/**
	 * Constructs the DataFormat from source.
	 *
	 * @param source - 1 character long data format (e.g. A for Alphanumeric)
	 * @return the typed DataFormat
	 */
	public static DataFormat fromSource(String source)
	{
		switch (source)
		{
			case "A":
				return ALPHANUMERIC;
			case "B":
				return BINARY;
			case "C":
				return CONTROL;
			case "D":
				return DATE;
			case "F":
				return FLOAT;
			case "I":
				return INTEGER;
			case "L":
				return LOGIC;
			case "N":
				return NUMERIC;
			case "P":
				return PACKED;
			case "T":
				return TIME;
			case "U":
				return UNICODE;
			default:
				throw new NaturalParseException(String.format("Can't determine DataFormat from format \"%s\"", source));
		}
	}
}
