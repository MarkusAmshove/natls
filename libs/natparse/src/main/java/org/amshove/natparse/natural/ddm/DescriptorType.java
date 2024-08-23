package org.amshove.natparse.natural.ddm;

import org.amshove.natparse.NaturalParseException;

public enum DescriptorType
{
	NONE,
	DESCRIPTOR,
	_S_DESCRIPTOR,
	SUPERDESCRIPTOR,
	SUBDESCRIPTOR,
	PHONETIC,
	UNIQUE,
	HYPERDESCRIPTOR,
	NONDESCRIPTOR;

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
			case "S" -> _S_DESCRIPTOR;
			case "P" -> PHONETIC;
			case "U" -> UNIQUE;
			case "H" -> HYPERDESCRIPTOR;
			case "N" -> NONDESCRIPTOR;
			case " " -> NONE;
			default -> throw new NaturalParseException(String.format("Can't determine DescriptorType from \"%s\"", source));
		};
	}
}
