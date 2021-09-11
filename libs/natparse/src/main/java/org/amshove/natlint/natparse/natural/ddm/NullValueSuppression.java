package org.amshove.natlint.natparse.natural.ddm;

import org.amshove.natlint.natparse.NaturalParseException;

public enum NullValueSuppression
{
	NONE,
	NULL_SUPRESSION,
	FIXED_STORAGE;

	/**
	 * Constructs the {@link NullValueSuppression} from source.
	 *
	 * @param source - 1 character long data format (e.g. F for FIXED_STORAGE)
	 * @return the typed {@link NullValueSuppression}
	 */
	public static NullValueSuppression fromSource(String source)
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
