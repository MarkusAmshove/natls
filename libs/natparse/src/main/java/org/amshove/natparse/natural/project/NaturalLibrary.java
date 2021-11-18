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
	private final List<NaturalLibrary> steplibs = new ArrayList<>();
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

	public void addSteplib(NaturalLibrary steplib)
	{
		steplibs.add(steplib);
	}

	public List<NaturalLibrary> getSteplibs()
	{
		return steplibs;
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
}
