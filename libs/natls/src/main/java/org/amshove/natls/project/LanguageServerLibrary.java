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
	private final Map<String, LanguageServerFile> fileByReferableName;
	private final Map<String, LanguageServerFile> ddmsByReferableName;
	private final List<LanguageServerLibrary> stepLibs = new ArrayList<>();

	public LanguageServerLibrary(NaturalLibrary library, Map<String, LanguageServerFile> fileByReferableName, Map<String, LanguageServerFile> ddmsByReferableName)
	{
		this.library = library;
		this.fileByReferableName = fileByReferableName;
		this.ddmsByReferableName = ddmsByReferableName;
		fileByReferableName.values().forEach(f -> f.setLibrary(this));
	}

	public String name()
	{
		return library.getName();
	}

	public static LanguageServerLibrary fromLibrary(NaturalLibrary library)
	{
		return new LanguageServerLibrary(
			library,
			library.files().stream().filter(f -> f.getFiletype() != NaturalFileType.DDM).collect(Collectors.toMap(NaturalFile::getReferableName, LanguageServerFile::fromFile)),
			library.files().stream().filter(f -> f.getFiletype() == NaturalFileType.DDM).collect(Collectors.toMap(NaturalFile::getReferableName, LanguageServerFile::fromFile))
		);
	}

	public LanguageServerFile findFile(NaturalFile naturalFile)
	{
		return fileByReferableName.get(naturalFile.getReferableName());
	}

	public LanguageServerFile findFile(Path path)
	{
		return fileByReferableName.values().stream().filter(f -> f.getPath().equals(path)).findFirst().orElse(null);
	}

	public List<LanguageServerFile> getModulesOfType(NaturalFileType type, boolean includeStepLibs)
	{
		var filesOfType = fileByReferableName.values().stream().filter(f -> f.getType() == type).collect(Collectors.toCollection(ArrayList::new));
		if (includeStepLibs)
		{
			stepLibs.forEach(l -> filesOfType.addAll(l.getModulesOfType(type, false)));
		}

		return filesOfType;
	}

	public Collection<LanguageServerFile> files()
	{
		return fileByReferableName.values();
	}

	LanguageServerFile provideNaturalModule(String referableName, boolean includeStepLibs)
	{
		if (fileByReferableName.containsKey(referableName))
		{
			return fileByReferableName.get(referableName);
		}

		if (includeStepLibs)
		{
			for (var stepLib : stepLibs)
			{
				var foundModule = stepLib.provideNaturalModule(referableName, false);
				if (foundModule != null)
				{
					return foundModule;
				}
			}
		}

		return null;
	}

	LanguageServerFile provideDdm(String referableName, boolean includeStepLibs)
	{
		if (ddmsByReferableName.containsKey(referableName))
		{
			return ddmsByReferableName.get(referableName);
		}

		if (includeStepLibs)
		{
			for (var stepLib : stepLibs)
			{
				var foundModule = stepLib.provideDdm(referableName, false);
				if (foundModule != null)
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

	public List<LanguageServerLibrary> getStepLibs()
	{
		return stepLibs;
	}

	/**
	 * Checks wether the file is within the library, determined by the Path. This can be used for files with haven't
	 * been indexed yet or are created at runtime.
	 */
	boolean residesInLibrary(Path path)
	{
		return path.startsWith(this.library.getSourcePath());
	}

	public void addFile(LanguageServerFile languageServerFile)
	{
		fileByReferableName.put(languageServerFile.getReferableName(), languageServerFile);
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

	public LanguageServerFile findFile(String referableName)
	{
		return fileByReferableName.get(referableName);
	}

	public void rename(LanguageServerFile oldFile, Path newPath)
	{
		fileByReferableName.remove(oldFile.getReferableName());

		var newName = newPath.getFileName().toString().split("\\.")[0];
		var oldLibrary = oldFile.getLibrary().getLibrary();
		var newNaturalFile = new NaturalFile(newName, newPath, oldFile.getType(), oldLibrary);
		oldLibrary.addFile(newNaturalFile);
		addFile(new LanguageServerFile(newNaturalFile));
	}
}
