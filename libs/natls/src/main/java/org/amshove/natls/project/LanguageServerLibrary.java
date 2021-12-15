package org.amshove.natls.project;

import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalLibrary;

import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

public class LanguageServerLibrary
{
	private final NaturalLibrary library;
	private final Map<String, LanguageServerFile> files;

	public LanguageServerLibrary(NaturalLibrary library, Map<String, LanguageServerFile> files)
	{
		this.library = library;
		this.files = files;
	}

	public String name()
	{
		return library.getName();
	}

	public static LanguageServerLibrary fromLibrary(NaturalLibrary library)
	{
		return new LanguageServerLibrary(library, library.files().stream().collect(Collectors.toMap(NaturalFile::getReferableName, LanguageServerFile::fromFile)));
	}

	public LanguageServerFile findFile(NaturalFile naturalFile)
	{
		return files.get(naturalFile.getReferableName());
	}

	public LanguageServerFile findFile(Path path)
	{
		return files.values().stream().filter(f -> f.getPath().equals(path)).findFirst().orElse(null);
	}
}
