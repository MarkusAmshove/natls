package org.amshove.natls.project;

import org.amshove.natls.TestProjectLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class ModuleReferenceParserShould
{
	@TempDir
	static Path projectDirectory;
	private static LanguageServerProject lspProject;

	@BeforeAll
	static void initialize()
	{
		lspProject = TestProjectLoader.loadProjectFromResources(projectDirectory, "modrefparser");
		var sut = new ModuleReferenceParser();
		lspProject.provideAllFiles().forEach(sut::parseReferences);
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
