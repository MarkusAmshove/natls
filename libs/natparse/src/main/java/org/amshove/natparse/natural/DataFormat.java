package org.amshove.natparse.natural;

import org.amshove.natparse.NaturalParseException;

import java.text.DecimalFormat;

public enum DataFormat
{
	ALPHANUMERIC('A'),
	BINARY('B'),
	CONTROL('C'),
	DATE('D'),
	FLOAT('F'),
	INTEGER('I'),
	LOGIC('L'),
	NUMERIC('N'),
	PACKED('P'),
	TIME('T'),
	UNICODE('U'),
	NONE(' ');

	private static final DecimalFormat LENGTH_FORMAT = new DecimalFormat("#.#");
	public static String formatLength(double length)
	{
		if(length == (long)length)
		{
			return "%d".formatted((long)length);
		}

		return LENGTH_FORMAT.format(length);
	}

	private final char identifier;

	DataFormat(char identifier)
	{
		this.identifier = identifier;
	}

	public char identifier()
	{
		return identifier;
	}

	/**
	 * Constructs the DataFormat from source.
	 *
	 * @param source - 1 character long data format (e.g. A for Alphanumeric)
	 * @return the typed DataFormat
	 */
	public static DataFormat fromSource(String source)
	{
		if(source.length() > 1 && !Character.isDigit(source.charAt(1)) && source.charAt(1) != '/') // TODO(lexermode): slash for array :(
		{
			throw new NaturalParseException(String.format("Can't determine DataFormat from format \"%s\"", source));
		}
		return fromSource(source.charAt(0));
	}

	public static DataFormat fromSource(char source)
	{
		return switch (Character.toUpperCase(source))
			{
				case 'A' -> ALPHANUMERIC;
				case 'B' -> BINARY;
				case 'C' -> CONTROL;
				case 'D' -> DATE;
				case 'F' -> FLOAT;
				case 'I' -> INTEGER;
				case 'L' -> LOGIC;
				case 'N' -> NUMERIC;
				case 'P' -> PACKED;
				case 'T' -> TIME;
				case 'U' -> UNICODE;
				case ' ' -> NONE;
				default -> throw new NaturalParseException(String.format("Can't determine DataFormat from format \"%s\"", source));
			};
	}
}
