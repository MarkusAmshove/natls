package org.amshove.natls.project;

import org.amshove.natls.languageserver.LspUtil;
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
		return libraries.get(naturalFile.getLibrary().getName()).findFilesByReferableName(naturalFile);
	}

	public LanguageServerFile findFile(Path filePath)
	{
		// TODO(perf): We could inspect the path and skip libraries
		for (var library : libraries.values())
		{
			for (var file : library.files())
			{
				if (file.getPath().equals(filePath))
				{
					return file;
				}
			}
		}

		return null;
	}

	// Used in tests only
	public LanguageServerFile findFileByReferableName(String library, String referableName)
	{
		return libraries.get(library).findFilesByReferableName(referableName).stream().findFirst().orElse(null);
	}

	// Used in tests only
	public LanguageServerFile findFileByReferableName(String referableName)
	{
		for (var lib : libraries.values())
		{
			var foundFiles = lib.findFilesByReferableName(referableName);
			if (foundFiles.size() > 1)
			{
				throw new RuntimeException("More than one file matches referable name \"%s\"".formatted(referableName));
			}
			if (!foundFiles.isEmpty())
			{
				return foundFiles.getFirst();
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

	public LanguageServerFile addFile(Path path)
	{
		var library = libraries.values().stream().filter(l -> l.residesInLibrary(path)).findFirst().orElseThrow();
		var naturalFile = new NaturalProjectFileIndexer().toNaturalFile(path);
		var lspFile = new LanguageServerFile(naturalFile);
		library.addFile(lspFile);
		return lspFile;
	}

	public void removeFile(LanguageServerFile file)
	{
		file.getLibrary().remove(file);
	}

	public Path rootPath()
	{
		return project.getRootPath();
	}

	public void renameFile(String oldUri, String newUri)
	{
		var oldFile = findFile(LspUtil.uriToPath(oldUri));
		oldFile.getLibrary().rename(oldFile, LspUtil.uriToPath(newUri));
	}

	public void renameReferableModule(String uri, String newReferableName)
	{
		var oldFile = findFile(LspUtil.uriToPath(uri));
		oldFile.getLibrary().rename(oldFile, newReferableName);
	}
}
