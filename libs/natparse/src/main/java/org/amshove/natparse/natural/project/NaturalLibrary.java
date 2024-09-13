package org.amshove.natparse.natural.project;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NaturalLibrary
{
	private final Path path;
	private final String libraryName;
	private final List<NaturalLibrary> stepLibs = new ArrayList<>();
	private final Map<String, List<NaturalFile>> modulesByReferableName = new HashMap<>();
	private final Map<String, NaturalFile> ddmsByReferableName = new HashMap<>();

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
		var files = new ArrayList<NaturalFile>();
		for (var modules : modulesByReferableName.values())
		{
			files.addAll(modules);
		}
		files.addAll(ddmsByReferableName.values());
		return files;
	}

	public void addFile(NaturalFile file)
	{
		if (file.getFiletype() == NaturalFileType.DDM)
		{
			ddmsByReferableName.put(file.getReferableName(), file);
		}
		else
		{
			modulesByReferableName.computeIfAbsent(file.getReferableName(), __ -> new ArrayList<>())
				.add(file);
		}
		file.setLibrary(this);
	}

	public void removeFile(NaturalFile file)
	{
		if (file.getFiletype() == NaturalFileType.DDM)
		{
			ddmsByReferableName.remove(file.getReferableName());
		}
		else
		{
			modulesByReferableName.computeIfAbsent(file.getReferableName(), __ -> new ArrayList<>())
				.remove(file);
		}

		file.setLibrary(null);
	}

	public NaturalFile findModuleByReferableName(String referableName, boolean includeStepLibs, NaturalFileType requestedType)
	{
		if (modulesByReferableName.containsKey(referableName))
		{
			if (requestedType != null)
			{
				for (var naturalFile : modulesByReferableName.get(referableName))
				{
					if (naturalFile.getFiletype() == requestedType)
					{
						return naturalFile;
					}
				}
			}
			return modulesByReferableName.get(referableName).getFirst();
		}

		if (includeStepLibs)
		{
			for (var stepLib : stepLibs)
			{
				var foundFile = stepLib.findModuleByReferableName(referableName, false, requestedType);
				if (foundFile != null)
				{
					return foundFile;
				}
			}
		}

		return null;
	}

	public NaturalFile findDdmByReferableName(String referableName, boolean includeStepLibs)
	{
		if (ddmsByReferableName.containsKey(referableName))
		{
			return ddmsByReferableName.get(referableName);
		}

		if (includeStepLibs)
		{
			for (var stepLib : stepLibs)
			{
				var foundFile = stepLib.findDdmByReferableName(referableName, false);
				if (foundFile != null)
				{
					return foundFile;
				}
			}
		}

		return null;
	}

	@Override
	public String toString()
	{
		return super.toString() + " " + libraryName;
	}
}
