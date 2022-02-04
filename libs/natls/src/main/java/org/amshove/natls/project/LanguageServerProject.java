package org.amshove.natls.project;

import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.natparse.natural.project.NaturalProjectFileIndexer;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LanguageServerProject
{
	private final NaturalProject project;
	private final Map<String, LanguageServerLibrary> libraries;

	private LanguageServerProject(NaturalProject project, List<LanguageServerLibrary> libraries)
	{
		this.project = project;
		this.libraries = libraries.stream().collect(Collectors.toMap(LanguageServerLibrary::name, l -> l));
		for (var lib : this.libraries.values())
		{
			lib.referenceStepLibs(this.libraries);
		}
	}

	public static LanguageServerProject fromProject(NaturalProject project)
	{
		return new LanguageServerProject(project, project.getLibraries().stream().map(LanguageServerLibrary::fromLibrary).toList());
	}

	public LanguageServerFile findFile(NaturalFile naturalFile)
	{
		return libraries.get(naturalFile.getLibrary().getName()).findFile(naturalFile);
	}

	// TODO: This shouldn't be here. The callers should use the LanguageServerFile as object provider
	//	  to find stuff that is actually in scope.
	public LanguageServerFile findFileByReferableName(String referableName)
	{
		for (var lib : libraries.values())
		{
			for(var file : lib.files())
			{
				if(file.getReferableName().equalsIgnoreCase(referableName))
				{
					return file;
				}
			}
		}

		return null;
	}

	public Collection<LanguageServerLibrary> libraries()
	{
		return libraries.values();
	}

	public Stream<LanguageServerFile> provideAllFiles()
	{
		return libraries.values().stream().flatMap(l -> l.files().stream());
	}

	public long countAllFiles()
	{
		return provideAllFiles().count();
	}

	public void addFile(Path path)
	{
		var library = libraries.values().stream().filter(l -> l.residesInLibrary(path)).findFirst().orElseThrow();
		var naturalFile = new NaturalProjectFileIndexer().toNaturalFile(path, library.getLibrary());
		library.addFile(new LanguageServerFile(naturalFile));
	}
}
