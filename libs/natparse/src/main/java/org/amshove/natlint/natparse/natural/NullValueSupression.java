package org.amshove.natlint.natparse.natural;

import org.amshove.natlint.natparse.NaturalParseException;

public enum NullValueSupression
{
	NONE,
	NULL_SUPRESSION,
	FIXED_STORAGE;

	/**
	 * Constructs the {@link NullValueSupression} from source.
	 *
	 * @param source - 1 character long data format (e.g. F for FIXED_STORAGE)
	 * @return the typed {@link NullValueSupression}
	 */
	public static NullValueSupression fromSource(String source)
	{
		switch (source)
		{
			case "N":
				return NULL_SUPRESSION;
			case "F":
				return FIXED_STORAGE;
			case " ":
				return NONE;
			default:
				throw new NaturalParseException(String.format("Can't determine NullValueSupression from \"%s\"", source));
		}
	}
}
