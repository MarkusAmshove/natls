package org.amshove.natparse.natural.ddm;

import org.amshove.natparse.NaturalParseException;

public enum DescriptorType
{
	NONE,
	DESCRIPTOR,
	SUPERDESCRIPTOR,
	PHONETIC,
	UNIQUE;

	/**
	 * Constructs the {@link DescriptorType} from source.
	 *
	 * @param source - 1 character long data format (e.g. D for DESCRIPTOR)
	 * @return the typed {@link DescriptorType}
	 */
	public static DescriptorType fromSource(String source)
	{
		return switch (source)
			{
				case "D" -> DESCRIPTOR;
				case "S" -> SUPERDESCRIPTOR;
				case "P" -> PHONETIC;
				case "U" -> UNIQUE;
				case " " -> NONE;
				default -> throw new NaturalParseException(String.format("Can't determine DescriptorType from \"%s\"", source));
			};
	}
}
