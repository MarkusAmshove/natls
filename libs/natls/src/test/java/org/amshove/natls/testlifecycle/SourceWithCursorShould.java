package org.amshove.natls.testlifecycle;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class SourceWithCursorShould
{
	@Test
	void extractWithinASingleLine()
	{
		assertExtracted("${HI}$", "HI", 0, 0, 0, 2);
	}

	@Test
	void extractWithinASingleLineWithoutSelection()
	{
		assertExtracted("H${}$I", "HI", 0, 1, 0, 1);
	}

	@Test
	void extractOverMultipleLines()
	{
		assertExtracted(
			"""
			${WRITE 'Hello'
			WRITE 'World'}$""", """
			WRITE 'Hello'
			WRITE 'World'""",
			0,
			0,
			1,
			"WRITE 'World'".length()
		);
	}

	@Test
	void throwAnExceptionIfNoCursorPositionWasFoundOnSingleLineSource()
	{
		assertThatThrownBy(() -> SourceWithCursor.fromSourceWithCursor("Hi"))
			.isInstanceOf(SourceWithCursorExtractionException.class)
			.hasMessage("No cursor position found");
	}

	@Test
	void throwAnExceptionIfNoCursorPositionWasFoundOnMultiLineSource()
	{
		assertThatThrownBy(() -> SourceWithCursor.fromSourceWithCursor("Hi\nHo"))
			.isInstanceOf(SourceWithCursorExtractionException.class)
			.hasMessage("No cursor position found");
	}

	@Test
	void throwAnExceptionIfNoEndPositionIsFoundOnSingleLineSource()
	{
		assertThatThrownBy(() -> SourceWithCursor.fromSourceWithCursor("${Hi"))
			.isInstanceOf(SourceWithCursorExtractionException.class)
			.hasMessage("No end position of cursor found");
	}

	@Test
	void throwAnExceptionIfNoEndPositionIsFoundOnMultiLineSource()
	{
		assertThatThrownBy(() -> SourceWithCursor.fromSourceWithCursor("${Hi\nHello"))
			.isInstanceOf(SourceWithCursorExtractionException.class)
			.hasMessage("No end position of cursor found");
	}

	@Test
	void throwAnExceptionIfTheEndPositionIsEncounteredWithoutAStartPosition()
	{
		assertThatThrownBy(() -> SourceWithCursor.fromSourceWithCursor("Hi}$"))
			.isInstanceOf(SourceWithCursorExtractionException.class)
			.hasMessage("End position of cursor encountered before encountering start position");
	}

	@Test
	void throwAnExceptionIfMultipleCursorPositionsAreFoundInSingleLine()
	{
		assertThatThrownBy(() -> SourceWithCursor.fromSourceWithCursor("${H${i}$"))
			.isInstanceOf(SourceWithCursorExtractionException.class)
			.hasMessage("Multiple cursor start positions found");
	}

	@Test
	void throwAnExceptionIfMultipleCursorPositionsAreFoundInMultiLine()
	{
		assertThatThrownBy(() -> SourceWithCursor.fromSourceWithCursor("${H\n${i}$"))
			.isInstanceOf(SourceWithCursorExtractionException.class)
			.hasMessage("Multiple cursor start positions found");
	}

	@Test
	void throwAnExceptionIfMultipleCursorEndPositionsAreFoundInSingleLine()
	{
		assertThatThrownBy(() -> SourceWithCursor.fromSourceWithCursor("H${i}$}$"))
			.isInstanceOf(SourceWithCursorExtractionException.class)
			.hasMessage("Multiple cursor end positions found");
	}

	@Test
	void throwAnExceptionIfMultipleCursorEndPositionsAreFoundInMultiLine()
	{
		assertThatThrownBy(() -> SourceWithCursor.fromSourceWithCursor("H${i}$\n}$"))
			.isInstanceOf(SourceWithCursorExtractionException.class)
			.hasMessage("Multiple cursor end positions found");
	}

	private static void assertExtracted(String source, String expectedExtractedSource, int startLine, int startChar, int endLine, int endChar)
	{
		var extracted = SourceWithCursor.fromSourceWithCursor(source);
		var cursor = extracted.cursorPosition();
		assertAll(
			"Extracted source with cursor <%s> does not match expected result".formatted(cursor),
			() -> assertThat(extracted.source()).isEqualTo(expectedExtractedSource),
			() -> assertThat(cursor.getStart().getLine()).as("Start line did not match").isEqualTo(startLine),
			() -> assertThat(cursor.getStart().getCharacter()).as("Start character did not match").isEqualTo(startChar),
			() -> assertThat(cursor.getEnd().getLine()).as("End line did not match").isEqualTo(endLine),
			() -> assertThat(cursor.getEnd().getCharacter()).as("End char did not match").isEqualTo(endChar)
		);
	}
}
