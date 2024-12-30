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
	private final Map<String, List<LanguageServerFile>> filesByReferableName;
	private final Map<String, LanguageServerFile> ddmsByReferableName;
	private final List<LanguageServerLibrary> stepLibs = new ArrayList<>();

	public LanguageServerLibrary(NaturalLibrary library, Map<String, List<LanguageServerFile>> filesByReferableName, Map<String, LanguageServerFile> ddmsByReferableName)
	{
		this.library = library;
		this.filesByReferableName = filesByReferableName;
		this.ddmsByReferableName = ddmsByReferableName;
		filesByReferableName.values().forEach(files -> files.forEach(f -> f.setLibrary(this)));
	}

	public String name()
	{
		return library.getName();
	}

	public static LanguageServerLibrary fromLibrary(NaturalLibrary library)
	{
		var filesByReferableName = library.files().stream()
			.filter(f -> f.getFiletype() != NaturalFileType.DDM)
			.map(LanguageServerFile::fromFile)
			.collect(Collectors.groupingBy(LanguageServerFile::getReferableName));

		var ddmsByReferableName = library.files().stream()
			.filter(f -> f.getFiletype() == NaturalFileType.DDM)
			.collect(Collectors.toMap(NaturalFile::getReferableName, LanguageServerFile::fromFile));

		return new LanguageServerLibrary(
			library,
			filesByReferableName,
			ddmsByReferableName
		);
	}

	public LanguageServerFile findFilesByReferableName(NaturalFile naturalFile)
	{
		return filesByReferableName.get(naturalFile.getReferableName()).stream()
			.filter(f -> f.getType() == naturalFile.getFiletype())
			.findFirst()
			.orElse(null);
	}

	public List<LanguageServerFile> getModulesOfType(NaturalFileType type, boolean includeStepLibs)
	{
		var filesOfType = filesByReferableName.values().stream().flatMap(Collection::stream).filter(files -> files.getType() == type).collect(Collectors.toCollection(ArrayList::new));
		if (includeStepLibs)
		{
			stepLibs.forEach(l -> filesOfType.addAll(l.getModulesOfType(type, false)));
		}

		return filesOfType;
	}

	public Collection<LanguageServerFile> files()
	{
		return filesByReferableName.values().stream().flatMap(Collection::stream).toList();
	}

	LanguageServerFile provideNaturalModule(String referableName, boolean includeStepLibs, NaturalFileType requestedType)
	{
		if (filesByReferableName.containsKey(referableName))
		{
			if (requestedType != null)
			{
				for (var file : filesByReferableName.get(referableName))
				{
					if (file.getType() == requestedType)
					{
						return file;
					}
				}
			}
			return filesByReferableName.get(referableName).getFirst();
		}

		if (includeStepLibs)
		{
			for (var stepLib : stepLibs)
			{
				var foundModule = stepLib.provideNaturalModule(referableName, false, requestedType);
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
		filesByReferableName.computeIfAbsent(languageServerFile.getReferableName(), __ -> new ArrayList<>())
			.add(languageServerFile);
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

	public List<LanguageServerFile> findFilesByReferableName(String referableName)
	{
		return filesByReferableName.computeIfAbsent(referableName, __ -> new ArrayList<>());
	}

	public void rename(LanguageServerFile oldFile, Path newPath)
	{

		var newName = newPath.getFileName().toString().split("\\.")[0];
		var oldFilesLsLibrary = oldFile.getLibrary();
		oldFilesLsLibrary.filesByReferableName.remove(oldFile.getReferableName());
		var oldNaturalLibrary = oldFilesLsLibrary.getLibrary();
		var newNaturalFile = new NaturalFile(newName, newPath, oldFile.getType(), oldNaturalLibrary);
		oldNaturalLibrary.removeFile(oldFile.getNaturalFile());
		oldNaturalLibrary.addFile(newNaturalFile);
		addFile(new LanguageServerFile(newNaturalFile));
	}

	public void rename(LanguageServerFile oldFile, String newReferableName)
	{
		var oldFilesLsLibrary = oldFile.getLibrary();
		oldFilesLsLibrary.filesByReferableName.remove(oldFile.getReferableName());
		var oldNaturalLibrary = oldFilesLsLibrary.getLibrary();
		var newNaturalFile = new NaturalFile(newReferableName, oldFile.getPath(), oldFile.getType(), oldNaturalLibrary);
		oldNaturalLibrary.removeFile(oldFile.getNaturalFile());
		oldNaturalLibrary.addFile(newNaturalFile);
		addFile(new LanguageServerFile(newNaturalFile));
	}

	public void remove(LanguageServerFile file)
	{
		file.getLibrary().library.removeFile(file.getNaturalFile());
		file.getLibrary().filesByReferableName.remove(file.getReferableName());
	}
}
