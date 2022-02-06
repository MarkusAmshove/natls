package org.amshove.natls.project;

import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.amshove.natparse.natural.project.NaturalLibrary;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LanguageServerLibrary
{
	private final NaturalLibrary library;
	private final Map<String, LanguageServerFile> files;
	private final List<LanguageServerLibrary> stepLibs = new ArrayList<>();

	public LanguageServerLibrary(NaturalLibrary library, Map<String, LanguageServerFile> files)
	{
		this.library = library;
		this.files = files;
		files.values().forEach(f -> f.setLibrary(this));
	}

	public String name()
	{
		return library.getName();
	}

	public static LanguageServerLibrary fromLibrary(NaturalLibrary library)
	{
		return new LanguageServerLibrary(
			library,
			library.files().stream().collect(Collectors.toMap(NaturalFile::getReferableName, LanguageServerFile::fromFile))
		);
	}

	public LanguageServerFile findFile(NaturalFile naturalFile)
	{
		return files.get(naturalFile.getReferableName());
	}

	public LanguageServerFile findFile(Path path)
	{
		return files.values().stream().filter(f -> f.getPath().equals(path)).findFirst().orElse(null);
	}

	public List<LanguageServerFile> getModulesOfType(NaturalFileType type, boolean includeStepLibs)
	{
		var filesOfType = files.values().stream().filter(f -> f.getType() == type).collect(Collectors.toCollection(ArrayList::new));
		if(includeStepLibs)
		{
			// TODO: Include steplibs
		}

		return filesOfType;
	}

	public Collection<LanguageServerFile> files()
	{
		return files.values();
	}

	LanguageServerFile provideNaturalFile(String referableName, boolean includeStepLibs)
	{
		if(files.containsKey(referableName))
		{
			return files.get(referableName);
		}

		if(includeStepLibs)
		{
			for (var stepLib : stepLibs)
			{
				var foundModule = stepLib.provideNaturalFile(referableName, false);
				if(foundModule != null)
				{
					return foundModule;
				}
			}
		}

		return null;
	}

	public void referenceStepLibs(Map<String, LanguageServerLibrary> libraries)
	{
		for (var stepLib : library.getStepLibs())
		{
			stepLibs.add(libraries.get(stepLib.getName()));
		}
	}

	/**
	 * Checks wether the file is within the library, determined by the Path.
	 * This can be used for files with haven't been indexed yet or are created
	 * at runtime.
	 */
	boolean residesInLibrary(Path path)
	{
		return path.startsWith(this.library.getSourcePath());
	}

	public void addFile(LanguageServerFile languageServerFile)
	{
		files.put(languageServerFile.getReferableName(), languageServerFile);
		languageServerFile.setLibrary(this);
		library.addFile(languageServerFile.getNaturalFile());
	}

	NaturalLibrary getLibrary()
	{
		return library;
	}

	public Path getSourcePath()
	{
		return library.getSourcePath();
	}
}
