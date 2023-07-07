package org.amshove.natls.codemutation;

import org.amshove.natls.testlifecycle.EmptyProjectTest;
import org.amshove.natparse.natural.VariableScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class CodeInsertionPlacerShould extends EmptyProjectTest
{
	private static final CodeInsertionPlacer sut = new CodeInsertionPlacer();

	@Test
	void findARangeToInsertALocalUsingWhenAUsingIsAlreadyPresent()
	{
		var file = createOrSaveLanguageServerFile("LIBONE", "PARAUSE.NSN", """
		DEFINE DATA
		LOCAL USING ASDF
		END-DEFINE
		END
		""");

		assertInsertion(
			sut.findRangeToInsertUsing(file, VariableScope.LOCAL),
			"",
			1, 0,
			1, 0,
			System.lineSeparator()
		);
	}

	@Test
	void findARangeToInsertALocalUsingWhenScopeIsAlreadyPresentInLineWithDefineData()
	{
		var file = createOrSaveLanguageServerFile("LIBONE", "PARAUSE.NSN", """
		DEFINE DATA LOCAL
		END-DEFINE
		END
		""");

		assertInsertion(
			sut.findRangeToInsertUsing(file, VariableScope.LOCAL),
			System.lineSeparator(),
			0, "DEFINE DATA ".length(),
			0, "DEFINE DATA ".length(),
			System.lineSeparator()
		);
	}

	@Test
	void findARangeToInsertALocalUsingWhenScopeIsAlreadyPresentInItsOwnLine()
	{
		var file = createOrSaveLanguageServerFile("LIBONE", "PARAUSE.NSN", """
		DEFINE DATA
		LOCAL
		END-DEFINE
		END
		""");

		assertInsertion(
			sut.findRangeToInsertUsing(file, VariableScope.LOCAL),
			"",
			1, 0,
			1, 0,
			System.lineSeparator()
		);
	}

	private void assertInsertion(CodeInsertion insertion, String prefix, int startLine, int offsetInStartLine, int endLine, int offsetInEndLine, String suffix)
	{
		var range = insertion.range();
		assertAll(
			"Expected insertion Range to be %d:%d to %d:%d with prefix <%s> and suffix <%s>".formatted(startLine, offsetInStartLine, endLine, offsetInEndLine, formatNl(prefix), formatNl(suffix)),
			() -> assertThat(range.getStart().getLine()).as("Start line").isEqualTo(startLine),
			() -> assertThat(range.getStart().getCharacter()).as("Offset in start line").isEqualTo(offsetInStartLine),
			() -> assertThat(range.getEnd().getLine()).as("End line").isEqualTo(endLine),
			() -> assertThat(range.getEnd().getCharacter()).as("Offset in end line").isEqualTo(offsetInEndLine),
			() -> assertThat(formatNl(insertion.insertionPrefix())).isEqualTo(formatNl(prefix)),
			() -> assertThat(formatNl(insertion.insertionSuffix())).isEqualTo(formatNl(suffix))
		);
	}

	private String formatNl(String string)
	{
		return string.replace(System.lineSeparator(), "\\n");
	}
}
