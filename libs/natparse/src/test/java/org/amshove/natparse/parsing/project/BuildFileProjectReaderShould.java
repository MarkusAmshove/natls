package org.amshove.natparse.parsing.project;

import org.amshove.natparse.infrastructure.IFilesystem;
import org.amshove.natparse.natural.project.NaturalProject;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class BuildFileProjectReaderShould
{

	private static final Path BUILD_FILE_PATH = Paths.get("some", "directory", "_naturalBuild");

	@Test
	void returnANaturalProjectContainingTheBasePath()
	{
		var fileSystem = new BuildFileBuilder(BUILD_FILE_PATH).toFileSystem();

		var naturalProject = createProject(fileSystem);
		assertThat(naturalProject.getRootPath()).isEqualTo(BUILD_FILE_PATH.getParent());
	}

	@Test
	void returnANaturalProjectContainingALibrary()
	{
		var fileSystem = new BuildFileBuilder(BUILD_FILE_PATH)
			.addLibrary("MYLIB")
			.toFileSystem();

		var naturalProject = createProject(fileSystem);
		assertThat(naturalProject.getLibraries().size()).isEqualTo(1);
		var naturalLibrary = naturalProject.getLibraries().get(0);
		assertThat(naturalLibrary.getName()).isEqualToIgnoringCase("MYLIB");
	}

	@Test
	void setTheSourcePathForLibraries()
	{
		var fileSystem = new BuildFileBuilder(BUILD_FILE_PATH)
			.addLibrary("MYLIB")
			.toFileSystem();

		var naturalProject = createProject(fileSystem);
		var naturalLibrary = naturalProject.getLibraries().get(0);
		assertThat(naturalLibrary.getSourcePath()).isEqualTo(sourceDirectory("MYLIB"));
	}

	@Test
	void notContainSystemLibraries()
	{
		var fileSystem = new BuildFileBuilder(BUILD_FILE_PATH)
			.addLibrary("MYLIB", "SYSERR") // SYSERR should be considered a system library because there will be no source directory
			.toFileSystem();

		var naturalProject = createProject(fileSystem);
		var naturalLibrary = naturalProject.getLibraries().get(0);
		assertThat(naturalLibrary.getStepLibs().size()).as("System library should not be contained").isEqualTo(0);
	}

	@Test
	void linkALibraryWithItsSteplib()
	{
		var fileSystem = new BuildFileBuilder(BUILD_FILE_PATH)
			.addLibrary("MYLIB", "SECONDLIB")
			.addLibrary("SECONDLIB")
			.toFileSystem();

		var naturalProject = createProject(fileSystem);
		var naturalLibrary = naturalProject.getLibraries().stream().filter(l -> l.getName().equals("MYLIB")).findFirst().orElseThrow(() -> new RuntimeException(""));
		assertThat(naturalLibrary.getStepLibs().size()).isEqualTo(1);
		assertThat(naturalLibrary.getStepLibs().get(0).getName()).isEqualTo("SECONDLIB");
	}

	private Path sourceDirectory(String name)
	{
		return Paths.get(BUILD_FILE_PATH.getParent().toString(), "Natural-Libraries", name);
	}

	private NaturalProject createProject(IFilesystem fileSystem)
	{
		return new BuildFileProjectReader(fileSystem).getNaturalProject(BUILD_FILE_PATH);
	}

}
