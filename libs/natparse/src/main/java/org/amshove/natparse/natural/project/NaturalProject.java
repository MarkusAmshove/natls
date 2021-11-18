package org.amshove.natparse.natural.project;

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
}
