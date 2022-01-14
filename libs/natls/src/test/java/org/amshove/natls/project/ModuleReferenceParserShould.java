package org.amshove.natls.project;

import org.amshove.natls.TestProjectLoader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class ModuleReferenceParserShould
{
	static Path projectDirectory;
	private static LanguageServerProject lspProject;

	@BeforeAll
	static void initialize() throws IOException
	{
		// TODO(junit): Use @TempDir when JUnit 5.8 is released
		projectDirectory = Files.createTempDirectory("referenceparsertest-");
		lspProject = TestProjectLoader.loadProjectFromResources(projectDirectory, "modrefparser");
		var sut = new ModuleReferenceParser();
		lspProject.provideAllFiles().forEach(sut::parseReferences);
	}

	@AfterAll
	static void cleanup() throws IOException
	{
		if(System.getenv().containsKey("GITHUB_ACTIONS"))
		{
			System.err.println("GITHUB ACTIONS");
			return;
		}

		try(var walk = Files.walk(projectDirectory))
		{
			walk
				.map(Path::toFile)
				.sorted((o1, o2) -> -o1.compareTo(o2))
				.forEach(File::delete);
		}
	}

	@Test
	void resolveExternalSubroutinesWithinTheSameLibrary()
	{
		assertThatFileReferences(module("SUB"), module("MY-EXTERNAL"));
	}

	@Test
	void resolveACopyCodeOverLibraryBorders()
	{
		assertThatFileReferences(module("SUB"), module("CCODE"));
	}

	@Test
	void resolveLocalDataAreasFromADifferentLibrary()
	{
		assertThatFileReferences(module("SUB"), module("MYLDA"));
	}

	@Test
	void resolveCallnats()
	{
		assertThatFileReferences(module("SUB"), module("SUB2"));
	}

	@Test
	void resolveFetchReturn()
	{
		assertThatFileReferences(module("PROG1"), module("PROG2"));
	}

	@Test
	void resolveFetchRepeat()
	{
		assertThatFileReferences(module("PROG2"), module("PROG3"));
	}

	@Test
	void resolveFunctionCalls()
	{
		assertThatFileReferences(module("PROG1"), module("ISTRUE"))	;
	}

	private void assertThatFileReferences(LanguageServerFile fileWithOutgoingReference, LanguageServerFile fileWithIncomingReference)
	{
		assertThat(fileWithOutgoingReference.getOutgoingReferences())
			.as("Outgoing reference to %s not found".formatted(fileWithIncomingReference.getReferableName()))
			.contains(fileWithIncomingReference);

		assertThat(fileWithIncomingReference.getIncomingReferences())
			.as("Incoming reference from %s not found".formatted(fileWithOutgoingReference.getReferableName()))
			.contains(fileWithOutgoingReference);
	}

	private LanguageServerFile module(String referableName)
	{
		var file = lspProject.findFileByReferableName(referableName);
		if (file == null)
		{
			throw new RuntimeException("Could not find file with referable name " + referableName);
		}

		return file;
	}
}
