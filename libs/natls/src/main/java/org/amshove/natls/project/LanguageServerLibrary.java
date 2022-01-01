package org.amshove.natls.project;

import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.amshove.natparse.natural.project.NaturalLibrary;
import org.amshove.natparse.parsing.IModuleProvider;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LanguageServerLibrary implements IModuleProvider
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

	@Override
	public INaturalModule findNaturalModule(String referableName)
	{
		return provideNaturalModule(referableName, true);
	}

	private INaturalModule provideNaturalModule(String referableName, boolean includeStepLibs)
	{
		if(files.containsKey(referableName))
		{
			return files.get(referableName).module();
		}

		if(includeStepLibs)
		{
			for (var stepLib : stepLibs)
			{
				var foundModule = stepLib.provideNaturalModule(referableName, false);
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
}
