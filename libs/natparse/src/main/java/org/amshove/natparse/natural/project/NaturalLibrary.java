package org.amshove.natparse.natural.project;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NaturalLibrary
{
	private final Path path;
	private final String libraryName;
	private final List<NaturalLibrary> stepLibs = new ArrayList<>();
	private final Map<String, NaturalFile> files = new HashMap<>();

	public NaturalLibrary(Path path)
	{
		this.path = path;
		libraryName = path.getFileName().toString();
	}

	public String getName()
	{
		return libraryName;
	}

	public Path getSourcePath()
	{
		return path;
	}

	public void addStepLib(NaturalLibrary stepLib)
	{
		stepLibs.add(stepLib);
	}

	public List<NaturalLibrary> getStepLibs()
	{
		return stepLibs;
	}

	public List<NaturalFile> files()
	{
		return List.copyOf(files.values());
	}

	public void addFile(NaturalFile file)
	{
		files.put(file.getReferableName(), file);
		file.setLibrary(this);
	}

	public NaturalFile findFileByReferableName(String referableName, boolean includeStepLibs)
	{
		if (files.containsKey(referableName))
		{
			return files.get(referableName);
		}

		if (includeStepLibs)
		{
			for (var stepLib : stepLibs)
			{
				var foundFile = stepLib.findFileByReferableName(referableName, false);
				if (foundFile != null)
				{
					return foundFile;
				}
			}
		}

		return null;
	}

	public List<NaturalFile> getModulesOfType(NaturalFileType type, boolean includeStepLibs)
	{
		var filesOfType = files.values().stream().filter(f -> f.getFiletype() == type).collect(Collectors.toCollection(ArrayList::new));
		if (includeStepLibs)
		{
			stepLibs.forEach(l -> filesOfType.addAll(l.getModulesOfType(type, false)));
		}

		return filesOfType;
	}

	@Override
	public String toString()
	{
		return super.toString() + " " + libraryName;
	}
}
