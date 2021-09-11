package org.amshove.natlint.natparse.natural.ddm;

import org.amshove.natlint.natparse.NaturalParseException;

public enum FieldType
{
	GROUP,
	MULTIPLE,
	PERIODIC,
	NONE;

	/**
	 * Constructs the {@link FieldType} from source.
	 *
	 * @param source - 1 character long field type (e.g. G for GROUP)
	 * @return the typed {@link FieldType}
	 */
	public static FieldType fromSource(String source)
	{
		switch (source)
		{
			case "G":
				return GROUP;
			case "M":
				return MULTIPLE;
			case "P":
				return PERIODIC;
			case " ":
				return NONE;
			default:
				throw new NaturalParseException(String.format("Can't determine FieldType from \"%s\"", source));
		}
	}
}
