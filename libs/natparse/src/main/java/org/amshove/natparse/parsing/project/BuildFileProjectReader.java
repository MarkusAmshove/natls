package org.amshove.natparse.parsing.project;

import org.amshove.natparse.infrastructure.ActualFilesystem;
import org.amshove.natparse.infrastructure.IFilesystem;
import org.amshove.natparse.natural.project.NaturalLibrary;
import org.amshove.natparse.natural.project.NaturalProject;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class BuildFileProjectReader
{

	private final IFilesystem filesystem;

	public BuildFileProjectReader()
	{
		this.filesystem = new ActualFilesystem();
	}

	public BuildFileProjectReader(IFilesystem filesystem)
	{
		this.filesystem = filesystem;
	}

	public NaturalProject getNaturalProject(Path buildfilePath)
	{
		var buildFileParser = new NaturalBuildFileParser(filesystem);
		var naturalLibraries = buildFileParser.parseLibraries(buildfilePath);

		var sourceDirectories = getSourceDirectories(buildfilePath);
		removeSystemLibraries(naturalLibraries, sourceDirectories);
		addSourcePaths(naturalLibraries, sourceDirectories);

		var actualLibraries = mapXmlLibraries(naturalLibraries);
		return new NaturalProject(buildfilePath.getParent(), actualLibraries);
	}

	private void addSourcePaths(List<XmlNaturalLibrary> naturalLibraries, List<Path> sourceDirectories)
	{
		for (var sourceDirectory : sourceDirectories)
		{
			var directoryName = sourceDirectory.toFile().getName();
			naturalLibraries.stream().filter(l -> l.getName().equalsIgnoreCase(directoryName)).findFirst().ifPresent(l -> l.setSourcePath(sourceDirectory));
		}
	}

	private void removeSystemLibraries(List<XmlNaturalLibrary> naturalLibraries, List<Path> sourceDirectories)
	{
		var nonSystemLibraries = sourceDirectories.stream().map(p -> p.toFile().getName()).collect(Collectors.toList());

		// Remove system-steplibs
		for (var naturalLibrary : naturalLibraries)
		{
			naturalLibrary.getSteplibs().removeIf(l -> !nonSystemLibraries.contains(l));
		}
		naturalLibraries.removeIf(l -> !nonSystemLibraries.contains(l.getName()));
	}

	private List<Path> getSourceDirectories(Path buildfilePath)
	{
		return filesystem.listDirectories(buildfilePath.getParent().resolve("Natural-Libraries"));
	}

	private List<NaturalLibrary> mapXmlLibraries(List<XmlNaturalLibrary> libraries)
	{
		var libraryMap = libraries.stream().collect(Collectors.toMap(XmlNaturalLibrary::getName, l -> new NaturalLibrary(l.getSourcePath())));

		for (var library : libraries)
		{
			var theLibrary = libraryMap.get(library.getName());

			for (var stepLib : library.getSteplibs())
			{
				theLibrary
					.addStepLib(libraryMap.get(stepLib));
			}

			if (libraryMap.containsKey("SYSTEM"))
			{
				theLibrary.addStepLib(libraryMap.get("SYSTEM"));
			}
		}

		return List.copyOf(libraryMap.values());
	}
}
