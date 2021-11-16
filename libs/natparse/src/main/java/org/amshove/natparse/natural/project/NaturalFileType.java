package org.amshove.natparse.natural.project;

import java.nio.file.Path;

public enum NaturalFileType
{
	SUBPROGRAM("NSN"),
	PROGRAM("NSP"),
	SUBROUTINE("NSS"),
	LDA("NSL"),
	DDM("NSD");

	public static final NaturalFileType[] VALUES = NaturalFileType.values();

	private final String extension;

	NaturalFileType(String extension)
	{
		this.extension = extension;
	}

	public static NaturalFileType fromExtension(String extension)
	{
		return switch (extension) {
			case "NSN" -> SUBPROGRAM;
			case "NSP" -> PROGRAM;
			case "NSS" -> SUBROUTINE;
			case "NSL" -> LDA;
			case "NSD" -> DDM;
			default -> throw new RuntimeException("Dunno");
		};
	}

	public String getExtension()
	{
		return extension;
	}

	public boolean matches(Path path)
	{
		return path.getFileName().toString().endsWith("." + extension);
	}
}
