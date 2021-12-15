package org.amshove.natls.project;

import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalProject;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LanguageServerProject
{
	private final NaturalProject project;
	private final Map<String, LanguageServerLibrary> libraries;

	private LanguageServerProject(NaturalProject project, List<LanguageServerLibrary> libraries)
	{
		this.project = project;
		this.libraries = libraries.stream().collect(Collectors.toMap(LanguageServerLibrary::name, l -> l));
	}

	public static LanguageServerProject fromProject(NaturalProject project)
	{
		return new LanguageServerProject(project, project.getLibraries().stream().map(LanguageServerLibrary::fromLibrary).toList());
	}

	public LanguageServerFile findFile(NaturalFile naturalFile)
	{
		return libraries.get(naturalFile.getLibrary().getName()).findFile(naturalFile);
	}
}
