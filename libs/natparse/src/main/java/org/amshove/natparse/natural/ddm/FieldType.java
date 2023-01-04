package org.amshove.natparse.natural.ddm;

import org.amshove.natparse.NaturalParseException;

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
		return switch (source)
		{
			case "G" -> GROUP;
			case "M" -> MULTIPLE;
			case "P" -> PERIODIC;
			case " " -> NONE;
			default -> throw new NaturalParseException(String.format("Can't determine FieldType from \"%s\"", source));
		};
	}
}
