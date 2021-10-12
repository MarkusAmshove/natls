package org.amshove.natlint.natparse.lexing.text;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

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
        var scanner = new SourceTextScanner("abc");
		scanner.advance();
		assertThat(scanner.position()).isEqualTo(1);
	}

	@Test
	void advanceTheCurrentPositionByAGivenOffset()
	{
        var scanner = new SourceTextScanner("abc");
		scanner.advance(2);
		assertThat(scanner.position()).isEqualTo(2);
	}

	@ParameterizedTest(name = "\"{0}\" advanced by {1} should be \"{2}\"")
	@CsvSource(
	{ "test,2,s", "SOURCE,0,S", "Advance,6,e" })
	void peekTheCharacterAtCurrentOffset(String source, int advanceBy, char expectedCharacter)
	{
        var scanner = new SourceTextScanner(source);
		scanner.advance(advanceBy);
		assertThat(scanner.peek()).isEqualTo(expectedCharacter);
	}

	@Test
	void peekTheEndCharacterWhenOutOfBounds()
	{
        var scanner = new SourceTextScanner("a");
		scanner.advance(2);
		assertThat(scanner.peek()).isEqualTo(SourceTextScanner.END_CHARACTER);
	}

	@ParameterizedTest(name = "\"{0}\" peeking offset {1} should be \"{2}\"")
	@CsvSource(
	{ "test,2,s", "SOURCE,0,S", "Advance,6,e" })
	void peekACharacterByAGivenOffset(String source, int peekOffset, char expectedCharacter)
	{
        var scanner = new SourceTextScanner(source);
		assertThat(scanner.peek(peekOffset)).isEqualTo(expectedCharacter);
	}

	@Test
	void peekTheEndCharacterWhenPeekingAnOffsetOutOfBounds()
	{
        var scanner = new SourceTextScanner("a");
		assertThat(scanner.peek(5)).isEqualTo(SourceTextScanner.END_CHARACTER);
	}

	@ParameterizedTest(name = "\"{0}\".advanceIf(\"{1}\") should result in position {2}")
	@CsvSource(
	{ "super source code,super source,12", "soUr,so,2", "even 1 with 2 numbers 3 long,even 1 with,11" })
	void advanceTheCurrentOffsetWhenMatchingAGivenText(String sourceText, String expectedText, int expectedEndPosition)
	{
        var scanner = new SourceTextScanner(sourceText);
		assertThat(scanner.position()).isEqualTo(0);
		assertThat(scanner.advanceIf(expectedText)).isEqualTo(true);
		assertThat(scanner.position()).isEqualTo(expectedEndPosition);
	}

	@Test
	void advanceIfTheExpectedTextIsEqualToSourceLength()
	{
        var scanner = new SourceTextScanner("ab");
		assertThat(scanner.advanceIf("ab")).isTrue();
	}

	@Test
	void advanceOverWindowsNewLineWhenCurrentIsUnixNewLine()
	{
        var scanner = new SourceTextScanner("\n\r\n\na");
		scanner.advance();
		scanner.advanceIf("\r\n");
		scanner.advance();
		assertThat(scanner.peek()).isEqualTo('a');
	}

	@Test
	void notAdvanceTheCurrentOffsetIfTheGivenTextIsNotMatched()
	{
        var scanner = new SourceTextScanner("super source code");
		assertThat(scanner.position()).isEqualTo(0);
		assertThat(scanner.advanceIf("code")).isEqualTo(false);
		assertThat(scanner.position()).isEqualTo(0);
	}

	@Test
	void notAdvanceTheCurrentOffsetIfTheGivenTextIsLongerThanTheSource()
	{
        var scanner = new SourceTextScanner("code");
		assertThat(scanner.position()).isEqualTo(0);
		assertThat(scanner.advanceIf("code ")).isEqualTo(false);
		assertThat(scanner.position()).isEqualTo(0);
	}

	@Test
	void returnTheCurrentLexemeStart()
	{
        var scanner = new SourceTextScanner("what is");
		scanner.start();
		scanner.advance(4);
		assertThat(scanner.lexemeStart()).isEqualTo(0);
	}

	@Test
	void returnTheCurrentLexemeLength()
	{
        var scanner = new SourceTextScanner("what is");
		scanner.start();
		scanner.advance(4);
		assertThat(scanner.lexemeLength()).isEqualTo(4);
	}

	@Test
	void resetTheCurrentLexemeStart()
	{
        var scanner = new SourceTextScanner("what is");
		scanner.advance(2);
		scanner.start();
		scanner.advance(3);
		assertThat(scanner.lexemeStart()).isEqualTo(2);
		scanner.reset();
		assertThat(scanner.lexemeStart()).isEqualTo(-1);
	}

	@Test
	void rollbackTheOffsetToCurrentLexemeStart()
	{
        var scanner = new SourceTextScanner("what is");

		scanner.advance(2);
		assertThat(scanner.peek()).isEqualTo('a');
		assertThat(scanner.position()).isEqualTo(2);

		scanner.start();
		scanner.advance(3);
		assertThat(scanner.peek()).isEqualTo('i');
		assertThat(scanner.position()).isEqualTo(5);

		scanner.rollbackCurrentLexeme();
		assertThat(scanner.position()).isEqualTo(2);
		assertThat(scanner.lexemeStart()).isEqualTo(-1);
	}

	@Test
	void throwAnExceptionIfThereIsNothingToRolLback()
	{
        var scanner = new SourceTextScanner("love");

		assertThatThrownBy(scanner::rollbackCurrentLexeme)
			.isInstanceOf(UnsupportedOperationException.class)
			.hasMessage("Can't reset offset if currentLexeme wasn't started");
	}

	@Test
	void returnTheLexemeText()
	{
        var scanner = new SourceTextScanner("Natural is a language");
		scanner.start();
		scanner.advance(7);
		assertThat(scanner.lexemeText()).isEqualTo("Natural");
	}

	@Test
	void recognizeWhenAtEnd()
	{
        var scanner = new SourceTextScanner("a");
		assertThat(scanner.isAtEnd()).isFalse();
		scanner.advance();
		assertThat(scanner.isAtEnd()).isTrue();
	}
}
