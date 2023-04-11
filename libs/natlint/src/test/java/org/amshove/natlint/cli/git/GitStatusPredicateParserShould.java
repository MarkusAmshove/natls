package org.amshove.natlint.cli.git;

import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class GitStatusPredicateParserShould
{
	@Test
	void createAPredicateForAddedFiles()
	{
		assertPredicates(
			new PredicateAssertion("?? .editorconfig", ".editorconfig")
		);
	}

	@Test
	void createAPredicateForModifiedFiles()
	{
		assertPredicates(
			new PredicateAssertion(" M Natural-Libraries/LIB/SRC/SUB.NSN", "Natural-Libraries/LIB/SRC/SUB.NSN")
		);
	}

	@Test
	void createNoPredicateForDeletedFiles()
	{
		assertPredicates(
			new PredicateAssertion(" D Natural-Libraries/LIB/SRC/SUB.NSN", "")
		);
	}

	@Test
	void createAPredicateForRenamedFiles()
	{
		assertPredicates(
			new PredicateAssertion(" R folder/SUB.NSN -> folder/SUB2.NSN", "folder/SUB2.NSN")
		);
	}

	@Test
	void createMultiplePredicates()
	{
		assertPredicates(
			new PredicateAssertion(" R folder/SUB.NSN -> folder/SUB2.NSN", "folder/SUB2.NSN"),
			new PredicateAssertion(" D Natural-Libraries/LIB/SRC/SUB.NSN", ""),
			new PredicateAssertion(" M Natural-Libraries/LIB/SRC/SUB.NSN", "Natural-Libraries/LIB/SRC/SUB.NSN"),
			new PredicateAssertion("?? .editorconfig", ".editorconfig")
		);
	}

	private void assertPredicates(PredicateAssertion... assertions)
	{
		var gitStatusLines = Arrays.stream(assertions).map(PredicateAssertion::gitStatus).toList();
		var predicates = new GitStatusPredicateParser().parseStatusToPredicates(gitStatusLines);

		var deleted = 0;
		for (var i = 0; i < assertions.length; i++)
		{
			var assertion = assertions[i];
			var file = new NaturalFileStub(assertion.expectedPath);
			if (assertion.gitStatus.trim().startsWith("D"))
			{
				assertThat(predicates).noneMatch(p -> p.test(file));
				deleted++;
				continue;
			}

			assertThat(predicates.get(i - deleted).test(file)).as("Predicate for %s did not match".formatted(assertion.expectedPath)).isTrue();
		}
	}

	private record PredicateAssertion(String gitStatus, String expectedPath)
	{

	}

	private static class NaturalFileStub extends NaturalFile
	{
		private final Path relativepath;

		public NaturalFileStub(String projectRelativePath)
		{
			super(projectRelativePath, Paths.get(projectRelativePath), NaturalFileType.COPYCODE, null);
			this.relativepath = Paths.get(projectRelativePath);
		}

		@Override
		public Path getProjectRelativePath()
		{
			return relativepath;
		}
	}
}
