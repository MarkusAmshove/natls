package org.amshove.natparse.parsing.project;

import org.amshove.natparse.infrastructure.IFilesystem;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BuildFileBuilder
{
	private final StringBuilder xml;
	private final Path buildFilePath;
	private final IFilesystem fileSystem;
	private final Set<Path> sourceFolders = new HashSet<>();
	private final Set<Path> includeSourceFolders = new HashSet<>();

	BuildFileBuilder(Path buildFilePath)
	{
		this.buildFilePath = buildFilePath;
		xml = new StringBuilder();
		xml.append("<LibrariesSteplibs>\n");
		fileSystem = mock(IFilesystem.class);
	}

	BuildFileBuilder addLibrary(String name, String... steplibs)
	{
		xml.append("<LibrarySteplib>\n");
		xml.append("<LibrarySteplibName>").append(name).append("</LibrarySteplibName>\n");

		var joinedSteplibs = String.join(";", steplibs);

		xml.append("<LibrarySteplibExtensions>").append(joinedSteplibs).append("</LibrarySteplibExtensions>\n");
		xml.append("</LibrarySteplib>\n");

		addSourceFolder(name);

		return this;
	}

	BuildFileBuilder addIncludeLibrary(String libName)
	{
		var includePath = buildFilePath.getParent().resolve("include");
		when(fileSystem.exists(includePath)).thenReturn(true);
		when(fileSystem.exists(includePath.resolve(libName))).thenReturn(true);
		includeSourceFolders.add(includePath.resolve(libName));
		when(fileSystem.listDirectories(includePath)).thenReturn(includeSourceFolders.stream().toList());
		return this;
	}

	private void addSourceFolder(String name)
	{
		var sourceFolderPath = buildFilePath.getParent().resolve("Natural-Libraries").resolve(name);
		sourceFolders.add(sourceFolderPath);
	}

	IFilesystem toFileSystem()
	{
		xml.append("</LibrariesSteplibs>");
		when(fileSystem.readFile(buildFilePath)).thenReturn(xml.toString());
		var sourceDirectories = new ArrayList<>(sourceFolders);
		when(fileSystem.listDirectories(buildFilePath.getParent().resolve("Natural-Libraries"))).thenReturn(sourceDirectories);
		return fileSystem;
	}
}
