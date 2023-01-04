package org.amshove.natparse.natural.project;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;

public class NaturalProject
{
	private final Path rootPath;
	private final List<NaturalLibrary> libraries;

	public NaturalProject(Path rootPath, List<NaturalLibrary> libraries)
	{
		this.rootPath = rootPath;
		this.libraries = libraries;
	}

	public Path getRootPath()
	{
		return rootPath;
	}

	public List<NaturalLibrary> getLibraries()
	{
		return libraries;
	}

	@Nullable
	public NaturalFile findModule(Path path)
	{
		// TODO(perf): We could inspect the path and skip libraries
		for (var library : libraries)
		{
			for (var file : library.files())
			{
				if (file.getPath().equals(path))
				{
					return file;
				}
			}
		}

		return null;
	}

	@Nullable
	public NaturalFile findModule(String moduleName)
	{
		for (var library : libraries)
		{
			for (var file : library.files())
			{
				if (file.getReferableName().equals(moduleName))
				{
					return file;
				}
			}
		}

		return null;
	}

	@Nullable
	public NaturalFile findModule(String libName, String moduleName)
	{
		for (var library : libraries)
		{
			if (!library.getName().equalsIgnoreCase(libName))
			{
				continue;
			}

			for (var file : library.files())
			{
				if (file.getReferableName().equalsIgnoreCase(moduleName))
				{
					return file;
				}
			}
		}

		return null;
	}
}
