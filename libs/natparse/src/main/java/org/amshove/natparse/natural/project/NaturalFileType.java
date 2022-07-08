package org.amshove.natparse.natural.project;

import java.nio.file.Path;
import java.util.Arrays;

public enum NaturalFileType
{
	SUBPROGRAM("NSN"),
	PROGRAM("NSP"),
	SUBROUTINE("NSS"),
	LDA("NSL"),
	DDM("NSD"),
	PDA("NSA"),
	GDA("NSG"),
	MAP("NSM"),
	COPYCODE("NSC"),
	FUNCTION("NS7");

	public static final NaturalFileType[] VALUES = NaturalFileType.values();

	private final String extension;

	NaturalFileType(String extension)
	{
		this.extension = extension;
	}

	public static NaturalFileType fromExtension(String extension)
	{
		return switch (extension)
			{
				case "NSN" -> SUBPROGRAM;
				case "NSP" -> PROGRAM;
				case "NSS" -> SUBROUTINE;
				case "NSL" -> LDA;
				case "NSD" -> DDM;
				case "NSA" -> PDA;
				case "NSM" -> MAP;
				case "NSG" -> GDA;
				case "NSC" -> COPYCODE;
				case "NS7" -> FUNCTION;
				default -> throw new RuntimeException(extension);
			};
	}

	public static boolean isNaturalFile(Path filepath)
	{
		return Arrays.stream(VALUES).anyMatch(t -> t.matches(filepath));
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
				case SUBPROGRAM, PROGRAM, SUBROUTINE, LDA, PDA, GDA, FUNCTION -> true;
				default -> false;
			};
	}

	public boolean canHaveBody()
	{
		return switch (this)
			{
				case SUBPROGRAM, PROGRAM, SUBROUTINE, FUNCTION, COPYCODE -> true;
				default -> false;
			};
	}
}
