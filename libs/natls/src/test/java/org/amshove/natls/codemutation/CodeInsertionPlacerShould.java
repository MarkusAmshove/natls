package org.amshove.natls.codemutation;

import org.amshove.natls.testlifecycle.EmptyProjectTest;
import org.amshove.natparse.natural.VariableScope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
			sut.findInsertionPositionToInsertUsing(file, VariableScope.LOCAL),
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
			sut.findInsertionPositionToInsertUsing(file, VariableScope.LOCAL),
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
			sut.findInsertionPositionToInsertUsing(file, VariableScope.LOCAL),
			"",
			1, 0,
			1, 0,
			System.lineSeparator()
		);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"LOCAL", "PARAMETER"
	})
	void findARangeToInsertAVariableWithScopeIfNoScopeIsPresent(VariableScope scope)
	{
		var file = createOrSaveLanguageServerFile("LIBONE", "PARAUSE.NSN", """
		DEFINE DATA
		END-DEFINE
		END
		""");

		assertInsertion(
			sut.findInsertionPositionToInsertVariable(file, scope),
			"%s%n".formatted(scope),
			1, 0,
			1, 0,
			System.lineSeparator()
		);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"LOCAL", "PARAMETER"
	})
	void findARangeToInsertAVariableWhenEmptyScopeIsPresent(VariableScope scope)
	{
		var file = createOrSaveLanguageServerFile("LIBONE", "PARAUSE.NSN", """
		DEFINE DATA
		%s
		END-DEFINE
		END
		""".formatted(scope));

		assertInsertion(
			sut.findInsertionPositionToInsertVariable(file, scope),
			"",
			2, 0,
			2, 0,
			System.lineSeparator()
		);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"LOCAL", "PARAMETER"
	})
	void findARangeToInsertAVariableWhenAnotherVariableIsPresent(VariableScope scope)
	{
		var file = createOrSaveLanguageServerFile("LIBONE", "PARAUSE.NSN", """
		DEFINE DATA
		%s
		1 #VAR (A10)
		END-DEFINE
		END
		""".formatted(scope));

		assertInsertion(
			sut.findInsertionPositionToInsertVariable(file, scope),
			"",
			2, 0,
			2, 0,
			System.lineSeparator()
		);
	}

	@Test
	void findARangeForLocalUsingsAfterParameterUsing()
	{
		var file = createOrSaveLanguageServerFile("LIBONE", "PARAUSE.NSN", """
		DEFINE DATA
		PARAMETER USING PDA
		END-DEFINE
		END
		""");

		assertInsertion(
			sut.findInsertionPositionToInsertUsing(file, VariableScope.LOCAL),
			"",
			2, 0,
			2, 0,
			System.lineSeparator()
		);
	}

	@Test
	void findARangeForLocalUsingsAfterParameter()
	{
		var file = createOrSaveLanguageServerFile("LIBONE", "PARAUSE.NSN", """
		DEFINE DATA
		PARAMETER
		1 #P-PARAM
		2 #P-PARM2 (A1)
		END-DEFINE
		END
		""");

		assertInsertion(
			sut.findInsertionPositionToInsertUsing(file, VariableScope.LOCAL),
			"",
			4, 0,
			4, 0,
			System.lineSeparator()
		);
	}

	@Test
	void findARangeForLocalVariablesAfterParameterUsing()
	{
		var file = createOrSaveLanguageServerFile("LIBONE", "PARAUSE.NSN", """
		DEFINE DATA
		PARAMETER USING PDAA
		END-DEFINE
		END
		""");

		assertInsertion(
			sut.findInsertionPositionToInsertUsing(file, VariableScope.LOCAL),
			"",
			2, 0,
			2, 0,
			System.lineSeparator()
		);
	}

	@Test
	void findARangeForLocalVariablesAfterParameter()
	{
		var file = createOrSaveLanguageServerFile("LIBONE", "PARAUSE.NSN", """
		DEFINE DATA
		PARAMETER USING PDAA
		PARAMETER
		1 #P-PARAM
		2 #P-PARM2 (A1)
		END-DEFINE
		END
		""");

		assertInsertion(
			sut.findInsertionPositionToInsertUsing(file, VariableScope.LOCAL),
			"",
			5, 0,
			5, 0,
			System.lineSeparator()
		);
	}

	@Test
	void findARangeForParameterUsingsBeforeLocal()
	{
		var file = createOrSaveLanguageServerFile("LIBONE", "PARAUSE.NSN", """
		DEFINE DATA
		LOCAL
		1 #VAR
		2 #VAR1 (A1)
		END-DEFINE
		END
		""");

		assertInsertion(
			sut.findInsertionPositionToInsertUsing(file, VariableScope.LOCAL),
			"",
			1, 0,
			1, 0,
			System.lineSeparator()
		);
	}

	@Test
	void findARangeForParameterVariablesBeforeLocal()
	{
		var file = createOrSaveLanguageServerFile("LIBONE", "PARAUSE.NSN", """
		DEFINE DATA
		LOCAL
		1 #VAR
		2 #VAR1 (A1)
		END-DEFINE
		END
		""");

		assertInsertion(
			sut.findInsertionPositionToInsertUsing(file, VariableScope.LOCAL),
			"",
			1, 0,
			1, 0,
			System.lineSeparator()
		);
	}

	@Test
	void findARangeForASubroutineInASubprogramWithEnd()
	{
		var file = createOrSaveLanguageServerFile("LIBONE", "SUBPROG.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			END
			""");

		assertInsertion(
			sut.findInsertionPositionForStatement(file),
			"",
			2, 0,
			2, 0,
			System.lineSeparator()
		);
	}

	@Test
	void findARangeForASubroutineInASubprogramWithEmptyBody()
	{
		var file = createOrSaveLanguageServerFile("LIBONE", "SUBPROG.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			""");

		assertInsertion(
			sut.findInsertionPositionForStatement(file),
			System.lineSeparator(),
			1, "END-DEFINE".length(),
			1, "END-DEFINE".length(),
			""
		);
	}

	@Test
	void findARangeForASubroutineInAnExternalSubroutine()
	{
		var file = createOrSaveLanguageServerFile("LIBONE", "SUBR.NSS", """
			DEFINE DATA LOCAL
			END-DEFINE
			DEFINE SUBROUTINE SUBR
			END-SUBROUTINE
			""");

		assertInsertion(
			sut.findInsertionPositionForStatement(file),
			"",
			3, 0,
			3, 0,
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
			() -> assertThat(formatNl(insertion.insertionPrefix())).as("Prefix does not match").isEqualTo(formatNl(prefix)),
			() -> assertThat(formatNl(insertion.insertionSuffix())).as("Suffix does not match").isEqualTo(formatNl(suffix))
		);
	}

	private String formatNl(String string)
	{
		return string.replace(System.lineSeparator(), "\\n");
	}
}
