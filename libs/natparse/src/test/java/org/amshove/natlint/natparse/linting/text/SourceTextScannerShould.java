package org.amshove.natlint.natparse.linting.text;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SourceTextScannerShould
{
	@Test
	void returnItsCurrentPosition()
	{
		assertThat(new SourceTextScanner("a").position()).isEqualTo(0);
	}

	@Test
	void advanceTheCurrentPosition()
	{
		SourceTextScanner scanner = new SourceTextScanner("abc");
		scanner.advance();
		assertThat(scanner.position()).isEqualTo(1);
	}

	@Test
	void advanceTheCurrentPositionByAGivenOffset()
	{
		SourceTextScanner scanner = new SourceTextScanner("abc");
		scanner.advance(2);
		assertThat(scanner.position()).isEqualTo(2);
	}

	@ParameterizedTest(name = "{index} {0} advanced by {1} should be {2}")
	@CsvSource(
	{ "test,2,s", "SOURCE,0,S", "Advance,6,e" })
	void peekTheCharacterAtCurrentOffset(String source, int advanceBy, char expectedCharacter)
	{
		SourceTextScanner scanner = new SourceTextScanner(source);
		scanner.advance(advanceBy);
		assertThat(scanner.peek()).isEqualTo(expectedCharacter);
	}

	@Test
	void peekTheEndCharacterWhenOutOfBounds()
	{
		SourceTextScanner scanner = new SourceTextScanner("a");
		scanner.advance(2);
		assertThat(scanner.peek()).isEqualTo(SourceTextScanner.END_CHARACTER);
	}

	@ParameterizedTest(name = "{index} {0} peeking offset {1} should be {2}")
	@CsvSource(
	{ "test,2,s", "SOURCE,0,S", "Advance,6,e" })
	void peekACharacterByAGivenOffset(String source, int peekOffset, char expectedCharacter)
	{
		SourceTextScanner scanner = new SourceTextScanner(source);
		assertThat(scanner.peek(peekOffset)).isEqualTo(expectedCharacter);
	}

	@Test
	void peekTheEndCharacterWhenPeekingAnOffsetOutOfBounds()
	{
		SourceTextScanner scanner = new SourceTextScanner("a");
		assertThat(scanner.peek(5)).isEqualTo(SourceTextScanner.END_CHARACTER);
	}

	@ParameterizedTest(name = "{index} '{0}.advanceIf({1}) should result in position {2}")
	@CsvSource(
	{ "super source code,super source,12", "soUr,so,2", "even 1 with 2 numbers 3 long,even 1 with,11" })
	void advanceTheCurrentOffsetWhenMatchingAGivenText(String sourceText, String expectedText, int expectedEndPosition)
	{
		SourceTextScanner scanner = new SourceTextScanner(sourceText);
		assertThat(scanner.position()).isEqualTo(0);
		assertThat(scanner.advanceIf(expectedText)).isEqualTo(true);
		assertThat(scanner.position()).isEqualTo(expectedEndPosition);
	}

	@Test
	void notAdvanceTheCurrentOffsetIfTheGivenTextIsNotMatched()
	{
		SourceTextScanner scanner = new SourceTextScanner("super source code");
		assertThat(scanner.position()).isEqualTo(0);
		assertThat(scanner.advanceIf("code")).isEqualTo(false);
		assertThat(scanner.position()).isEqualTo(0);
	}

	@Test
	void notAdvanceTheCurrentOffsetIfTheGivenTextIsLongerThanTheSource()
	{
		SourceTextScanner scanner = new SourceTextScanner("code");
		assertThat(scanner.position()).isEqualTo(0);
		assertThat(scanner.advanceIf("code is cool")).isEqualTo(false);
		assertThat(scanner.position()).isEqualTo(0);
	}

	@Test
	void returnTheCurrentLexemeStart()
	{
		SourceTextScanner scanner = new SourceTextScanner("what is");
		scanner.start();
		scanner.advance(4);
		assertThat(scanner.lexemeStart()).isEqualTo(0);
	}

	@Test
	void returnTheCurrentLexemeLength()
	{
		SourceTextScanner scanner = new SourceTextScanner("what is");
		scanner.start();
		scanner.advance(4);
		assertThat(scanner.lexemeLength()).isEqualTo(4);
	}

	@Test
	void resetTheCurrentLexemeStart()
	{
		SourceTextScanner scanner = new SourceTextScanner("what is");
		scanner.advance(2);
		scanner.start();
		scanner.advance(3);
		assertThat(scanner.lexemeStart()).isEqualTo(2);
		scanner.reset();
		assertThat(scanner.lexemeStart()).isEqualTo(0);
	}

	@Test
	void returnTheLexemeText()
	{
		SourceTextScanner scanner = new SourceTextScanner("Natural is a language");
		scanner.start();
		scanner.advance(7);
		assertThat(scanner.lexemeText()).isEqualTo("Natural");
	}
}
