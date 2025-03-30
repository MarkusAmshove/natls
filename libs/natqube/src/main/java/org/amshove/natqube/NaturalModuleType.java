package org.amshove.natqube;

import org.sonar.api.batch.fs.InputFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum NaturalModuleType
{
	DDM("NSD"),
	SUBPROGRAM("NSN"),
	PROGRAM("NSP"),
	SUBROUTINE("NSS"),
	HELP_ROUTINE("NSH"),
	GDA("NSG"),
	LDA("NSL"),
	PDA("NSA"),
	MAP("NSM"),
	COPY_CODE("NSC"),
	FUNCTION("NS7"),
	TEXT("NST"),
	CLASS("NS4");

	private static final Map<String, NaturalModuleType> extensionToType = new HashMap<>();

	static
	{
		for (var type : NaturalModuleType.values())
		{
			extensionToType.put(type.extension, type);
		}
	}

	private final String extension;

	NaturalModuleType(String extension)
	{
		this.extension = extension;
	}

	public static NaturalModuleType fromInputFile(InputFile inputFile)
	{
		var filenameWithExtension = inputFile.filename();
		var extension = filenameWithExtension.substring(filenameWithExtension.lastIndexOf('.') + 1);
		return extensionToType.get(extension);
	}

	public static List<String> allFileExtensions()
	{
		return extensionToType.keySet().stream().toList();
	}
}
