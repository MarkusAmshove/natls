package org.amshove.natparse.parsing.project;

import org.amshove.natparse.infrastructure.ActualFilesystem;
import org.amshove.natparse.infrastructure.IFilesystem;
import org.amshove.natparse.natural.project.NaturalLibrary;
import org.amshove.natparse.natural.project.NaturalProject;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
		var xmlLibraryDefinitions = buildFileParser.parseLibraries(buildfilePath);

		var sourceDirectories = getSourceDirectories(buildfilePath);
		var includeDirectories = getIncludeDirectories(buildfilePath);

		removeSystemLibraries(xmlLibraryDefinitions, sourceDirectories, includeDirectories);
		addSourcePaths(xmlLibraryDefinitions, sourceDirectories);

		var actualLibraries = mapXmlLibraries(xmlLibraryDefinitions);
		addIncludeLibraries(actualLibraries, xmlLibraryDefinitions, includeDirectories);
		return new NaturalProject(buildfilePath.getParent(), List.copyOf(actualLibraries.values()));
	}

	private void addIncludeLibraries(Map<String, NaturalLibrary> naturalLibraries, List<XmlNaturalLibrary> xmlLibraries, List<Path> includePaths)
	{
		if (includePaths.isEmpty())
		{
			return;
		}

		var includeLibraries = includePaths.stream().map(NaturalLibrary::new).toList();
		for (var includeLibrary : includeLibraries)
		{
			if (naturalLibraries.containsKey(includeLibrary.getName()))
			{
				throw new BuildFileParserException("Can't add library include/%s when a library with the same name exists under Natural-Libraries/".formatted(includeLibrary.getName()));
			}

			naturalLibraries.put(includeLibrary.getName(), includeLibrary);
			// Go through the XML definitions again to see if the library from include/ is a steplib
			// that we can now provide.
			for (var xmlLibrary : xmlLibraries)
			{
				for (var steplib : xmlLibrary.getSteplibs())
				{
					if (steplib.equals(includeLibrary.getName()) && naturalLibraries.containsKey(steplib))
					{
						naturalLibraries.get(xmlLibrary.getName()).addStepLib(includeLibrary);
						break;
					}
				}
			}
		}
	}

	private void addSourcePaths(List<XmlNaturalLibrary> naturalLibraries, List<Path> sourceDirectories)
	{
		for (var sourceDirectory : sourceDirectories)
		{
			var directoryName = sourceDirectory.toFile().getName();
			naturalLibraries.stream().filter(l -> l.getName().equalsIgnoreCase(directoryName)).findFirst().ifPresent(l -> l.setSourcePath(sourceDirectory));
		}
	}

	private void removeSystemLibraries(List<XmlNaturalLibrary> naturalLibraries, List<Path> sourceDirectories, List<Path> includeDirectories)
	{
		var nonSystemLibraries = Stream.concat(sourceDirectories.stream(), includeDirectories.stream())
			.map(p -> p.toFile().getName())
			.toList();

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

	private List<Path> getIncludeDirectories(Path buildFilePath)
	{
		var includePath = buildFilePath.getParent().resolve("include");
		return filesystem.exists(includePath)
			? filesystem.listDirectories(includePath)
			: List.of();
	}

	private Map<String, NaturalLibrary> mapXmlLibraries(List<XmlNaturalLibrary> libraries)
	{
		var libraryMap = libraries.stream().collect(Collectors.toMap(XmlNaturalLibrary::getName, this::mapXmlLibrary));

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

		return libraryMap;
	}

	private NaturalLibrary mapXmlLibrary(XmlNaturalLibrary lib)
	{
		var sourcePathUnderNaturalLibraries = lib.getSourcePath();
		return new NaturalLibrary(sourcePathUnderNaturalLibraries);
	}
}
