package org.amshove.natlint.natparse.lexing;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@Nested
public class LexerShould extends AbstractLexerTest
{
	@Test
	void storeTheLineInformationOfTokens()
	{
		var lexer = new Lexer();
		var tokens = lexer.lex("abc\nabc");

		var firstAbc = tokens.get(0);
		assertThat(firstAbc.line()).isEqualTo(0);

		var lineEnd = tokens.get(1);
		assertThat(lineEnd.line()).isEqualTo(0);

		var secondAbc = tokens.get(2);
		assertThat(secondAbc.line()).isEqualTo(1);
	}

	@ParameterizedTest
	@CsvSource(
	{ "abc,3", "bbbbcda,7" })
	void storeTheLengthOfTokens(String source, int expectedLength)
	{
		var token = lexSingle(source);
		assertThat(token.length())
			.as("Expected token with length [%d] but was [%d]. Actual token: [%s]", expectedLength, token.length(), token.toString())
			.isEqualTo(expectedLength);
	}

	@ParameterizedTest
	@CsvSource(
	{ "abc,0,0", "abc cba,2,4" })
	void storeTheOffsetOfTokens(String source, int nthIndex, int expectedOffset)
	{
		var token = lexSingle(source, nthIndex);
		assertThat(token.offset())
			.as("Expected Token at index [%d] but was [%d]", expectedOffset, token.offset())
			.isEqualTo(expectedOffset);
	}

	@Test
	void storeTheOffsetInLineOfTokens()
	{
		assertThat(lexSingle("abc abc", 2).offsetInLine()).isEqualTo(4);
	}

	@Test
	void storeUnknownCharacters()
	{
		var lexer = new Lexer();
		lexer.lex("\u2412\u4123\u1234");
		var unknownCharacters = lexer.getUnknownCharacters();

		assertThat(unknownCharacters)
			.contains('\u2412', '\u4123', '\u1234');
	}
}
