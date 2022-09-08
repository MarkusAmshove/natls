package org.amshove.natparse.natural.project;

import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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

	public Optional<NaturalFile> findModule(Path path)
	{
		// TODO(perf): We could inspect the path and skip libraries
		for (var library : libraries)
		{
			for (var file : library.files())
			{
				if(file.getPath().equals(path))
				{
					return Optional.of(file);
				}
			}
		}

		return Optional.empty();
	}

	public Optional<NaturalFile> findModule(String moduleName)
	{
		for (var library : libraries)
		{
			for (var file : library.files())
			{
				if(file.getReferableName().equals(moduleName))
				{
					return Optional.of(file);
				}
			}
		}

		return Optional.empty();
	}

	/**
	 * Does the same as {@link NaturalProject#findModule(String)} but throws an exception if the module is not found.<br/>
	 * Can be used if you're 100% certain that it exists.
	 */
	public NaturalFile findModuleUnsafe(String moduleName)
	{
		return findModule(moduleName).orElseThrow(() -> new NoSuchElementException("Module with name %s not found in any library".formatted(moduleName)));
	}

	public Optional<NaturalFile> findModule(String libName, String moduleName)
	{
		for (var library : libraries)
		{
			if(!library.getName().equalsIgnoreCase(libName))
			{
				continue;
			}

			for (var file : library.files())
			{
				if(file.getReferableName().equalsIgnoreCase(moduleName))
				{
					return Optional.of(file);
				}
			}
		}

		return Optional.empty();
	}

	/**
	 * Tries to find the module in the given library with given name.<br/>
	 * Does the same thing as {@link NaturalProject#findModule(String, String)}, but throws an exception if nothing is found.<br/>
	 * Can be used if you're 100% certain that it exists.
	 */
	public NaturalFile findModuleUnsafe(String libName, String moduleName)
	{
		return findModule(libName, moduleName).orElseThrow(() -> new NoSuchElementException("Module %s.%s not found".formatted(libName, moduleName)));
	}
}
