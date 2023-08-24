package org.amshove.natparse.parsing.project;

import org.amshove.natparse.infrastructure.IFilesystem;
import org.amshove.natparse.natural.project.NaturalLibrary;
import org.amshove.natparse.natural.project.NaturalProject;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class BuildFileProjectReaderShould
{

	private static final Path PROJECT_ROOT = Paths.get("some", "directory");
	private static final Path BUILD_FILE_PATH = PROJECT_ROOT.resolve("_naturalBuild");

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

		var sourceDirectory = Paths.get(BUILD_FILE_PATH.getParent().toString(), "Natural-Libraries", "MYLIB");
		assertThat(naturalLibrary.getSourcePath()).isEqualTo(sourceDirectory);
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

	@Test
	void linkLibsWithSystemLibrary()
	{
		var fileSystem = new BuildFileBuilder(BUILD_FILE_PATH)
			.addLibrary("SYSTEM")
			.addLibrary("MYLIB", "SECONDLIB")
			.addLibrary("SECONDLIB")
			.toFileSystem();

		var naturalProject = createProject(fileSystem);
		var naturalLibrary = assertHasLibrary(naturalProject, "MYLIB");
		assertThat(naturalLibrary.getStepLibs().size()).isEqualTo(2);
		assertThat(naturalLibrary.getStepLibs().get(1).getName()).isEqualTo("SYSTEM");
	}

	@Test
	void notLinkLibrariesWithThemself()
	{
		var fileSystem = new BuildFileBuilder(BUILD_FILE_PATH)
			.addLibrary("SYSTEM")
			.addLibrary("MYLIB", "MYLIB")
			.toFileSystem();

		var project = createProject(fileSystem);
		var myLib = assertHasLibrary(project, "MYLIB");
		assertThat(myLib.getStepLibs()).as("Only one step lib to SYSTEM is expected").hasSize(1);
		assertThat(myLib.getStepLibs().get(0).getName()).isEqualTo("SYSTEM");

		var system = assertHasLibrary(project, "SYSTEM");
		assertThat(system.getStepLibs()).as("SYSTEM shouldn't reference itself").isEmpty();
	}

	@Test
	void createLibrariesThatArePresentInAnIncludeDirectoryNextToTheProjectFile()
	{
		// For example, SYSRPC is a library from SAG that is present on the server.
		// It is not itself a library in the project file, but set as steplib for other libraries.
		// We want those to be seen as library so that we can provide stub files in an include/ folder.

		var fileSystem = new BuildFileBuilder(BUILD_FILE_PATH)
			.addLibrary("MYLIB", "SYSRPC")
			.addIncludeLibrary("SYSRPC")
			.toFileSystem();

		var project = createProject(fileSystem);
		var myLib = assertHasLibrary(project, "MYLIB");
		assertThat(myLib.getSourcePath()).isEqualTo(PROJECT_ROOT.resolve("Natural-Libraries").resolve("MYLIB"));

		var sysRpc = assertHasLibrary(project, "SYSRPC");
		assertThat(sysRpc.getSourcePath()).isEqualTo(PROJECT_ROOT.resolve("include").resolve("SYSRPC"));
		assertThat(myLib.getStepLibs()).contains(sysRpc);
	}

	private NaturalProject createProject(IFilesystem fileSystem)
	{
		return new BuildFileProjectReader(fileSystem).getNaturalProject(BUILD_FILE_PATH);
	}

	private NaturalLibrary assertHasLibrary(NaturalProject project, String library)
	{
		return project.getLibraries().stream().filter(l -> l.getName().equals(library)).findFirst().orElseThrow();
	}

}
