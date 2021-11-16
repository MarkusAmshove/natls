package org.amshove.natparse.natural.project;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class NaturalLibrary
{
	private final Path path;
	private final String libraryName;
	private List<NaturalLibrary> steplibs = new ArrayList<>();

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

	public void addSteplib(NaturalLibrary steplib)
	{
		steplibs.add(steplib);
	}

	public List<NaturalLibrary> getSteplibs()
	{
		return steplibs;
	}
}
