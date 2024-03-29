package org.amshove.natparse.natural.project;

import org.amshove.natparse.NaturalParseException;

import java.nio.file.Path;
import java.util.Arrays;

public enum NaturalFileType
{
	DDM("NSD"),
	SUBPROGRAM("NSN"),
	PROGRAM("NSP"),
	SUBROUTINE("NSS"),
	HELPROUTINE("NSH"),
	GDA("NSG"),
	LDA("NSL"),
	PDA("NSA"),
	MAP("NSM"),
	COPYCODE("NSC"),
	FUNCTION("NS7");
	// TEXT
	// CLASS NS4
	// ADAPTER
	// RESOURCE

	public static final NaturalFileType[] VALUES = NaturalFileType.values();

	public static NaturalFileType fromPath(Path filepath)
	{
		var wholeName = filepath.getFileName().toString();
		var split = wholeName.split("\\.");
		if (split.length < 2)
		{
			throw new NaturalParseException("Could not determine natural file type from path <%s>".formatted(filepath));
		}
		return fromExtension(split[split.length - 1]);
	}

	public static NaturalFileType fromExtension(String extension)
	{
		return switch (extension)
		{
			case "NSD" -> DDM;
			case "NSN" -> SUBPROGRAM;
			case "NSP" -> PROGRAM;
			case "NSS" -> SUBROUTINE;
			case "NSH" -> HELPROUTINE;
			case "NSG" -> GDA;
			case "NSL" -> LDA;
			case "NSA" -> PDA;
			case "NSM" -> MAP;
			case "NSC" -> COPYCODE;
			case "NS7" -> FUNCTION;
			default -> throw new NaturalParseException("Could not determine natural file type by extension %s".formatted(extension));
		};
	}

	public static boolean isNaturalFile(Path filepath)
	{
		return Arrays.stream(VALUES).anyMatch(t -> t.matches(filepath));
	}

	private final String extension;

	NaturalFileType(String extension)
	{
		this.extension = extension;
	}

	public String getExtension()
	{
		return extension;
	}

	public boolean matches(Path path)
	{
		return path.getFileName().toString().endsWith("." + extension);
	}

	public boolean canHaveDefineData()
	{
		return switch (this)
		{
			case SUBPROGRAM, PROGRAM, SUBROUTINE, HELPROUTINE, LDA, PDA, GDA, FUNCTION, MAP -> true;
			default -> false;
		};
	}

	public boolean canHaveBody()
	{
		return switch (this)
		{
			case SUBPROGRAM, PROGRAM, SUBROUTINE, HELPROUTINE, FUNCTION, COPYCODE, MAP -> true;
			default -> false;
		};
	}
}
