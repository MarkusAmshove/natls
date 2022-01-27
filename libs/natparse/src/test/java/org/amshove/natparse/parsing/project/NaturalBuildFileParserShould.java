package org.amshove.natparse.parsing.project;

import org.amshove.natparse.infrastructure.IFilesystem;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;

import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NaturalBuildFileParserShould {

	private static final Path TEST_BUILDFILE = Paths.get("/some/directory/_naturalBuild");

	@Test
	void findANaturalLibraryFromABuildFile() {
		IFilesystem fileSystem = new BuildFileBuilder(TEST_BUILDFILE)
				.addLibrary("FIRSTLIB")
				.toFileSystem();

		var sut = new NaturalBuildFileParser(fileSystem);
		var libraries = sut.parseLibraries(TEST_BUILDFILE);
		assertThat(libraries).hasSize(1);
		AssertionsForClassTypes.assertThat(libraries.get(0).getName()).isEqualTo("FIRSTLIB");
	}

	@Test
	void findANaturalLibraryWithSteplibs() {
		IFilesystem fileSystem = new BuildFileBuilder(TEST_BUILDFILE)
				.addLibrary("FIRSTLIB", "SECONDLIB")
				.addLibrary("SECONDLIB")
				.toFileSystem();

		var naturalLibraries = parseBuildFile(fileSystem);
		assertThat(naturalLibraries).hasSize(2);
		assertLibraryContainsSteplibs(naturalLibraries.get(0), "SECONDLIB");
		AssertionsForClassTypes.assertThat(naturalLibraries.get(1).getName())
				.isEqualToIgnoringCase("SECONDLIB");
	}

	@Test
	void findNaturalLibrariesWithRingDependencies() {
		IFilesystem fileSystem = new BuildFileBuilder(TEST_BUILDFILE)
				.addLibrary("FIRSTLIB", "SECONDLIB")
				.addLibrary("SECONDLIB", "FIRSTLIB")
				.toFileSystem();

		var naturalLibraries = parseBuildFile(fileSystem);
		assertLibraryContainsSteplibs(naturalLibraries.get(0), "SECONDLIB");
		assertLibraryContainsSteplibs(naturalLibraries.get(1), "FIRSTLIB");
	}

	@Test
	void throwAnExceptionWhenTheBuildFileCouldNotBeRead() {
		var fileSystem = mock(IFilesystem.class);
		when(fileSystem.readFile(TEST_BUILDFILE)).thenReturn("<nonvalidxmlfile>");

		assertThat(catchThrowable(() -> parseBuildFile(fileSystem)))
				.isInstanceOf(BuildFileParserException.class);
	}

	private void assertLibraryContainsSteplibs(XmlNaturalLibrary library, String... steplibs) {
		var assertions = AssertionsForClassTypes.assertThat(library);
		for (var steplib : steplibs) {
			assertions.matches(containsSteplib(steplib), "Expected library " + library.getName() + " to contain Steplib " + steplib);
		}
	}

	private Predicate<? super XmlNaturalLibrary> containsSteplib(String secondlib) {
		return naturalLibrary -> naturalLibrary.getSteplibs().stream().anyMatch(l -> l.equalsIgnoreCase(secondlib));
	}

	private List<XmlNaturalLibrary> parseBuildFile(IFilesystem fileSystem) {
		var sut = new NaturalBuildFileParser(fileSystem);
		return sut.parseLibraries(TEST_BUILDFILE);
	}
}
