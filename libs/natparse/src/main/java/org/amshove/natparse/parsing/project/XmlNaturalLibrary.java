package org.amshove.natparse.parsing.project;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class XmlNaturalLibrary
{
	private final String name;
	private final Set<String> steplibs = new HashSet<>();
	private Path sourcePath;

	public XmlNaturalLibrary(String name)
	{
		this.name = name;
	}

	public void addSteplib(String name)
	{
		if(name.equals(this.name))
		{
			return;
		}

		steplibs.add(name);
	}

	public Set<String> getSteplibs()
	{
		return steplibs;
	}

	public String getName()
	{
		return name;
	}

	public void setSourcePath(Path sourceDirectory)
	{
		sourcePath = sourceDirectory;
	}

	public Path getSourcePath()
	{
		return sourcePath;
	}
}
