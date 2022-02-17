package org.amshove.natparse.lexing;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@Nested
public class LexerShould extends AbstractLexerTest
{
	@Test
	void storeTheLineInformationOfTokens()
	{
		var lexer = new Lexer();
		var tokens = lexer.lex("abc\nabc", Paths.get("TEST.NSN"));

		var firstAbc = tokens.peek();
		assertThat(firstAbc.line()).isEqualTo(0);

		var secondAbc = tokens.peek(1);
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
		{ "abc,0,0", "abc cba,1,4" })
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
		assertThat(lexSingle("abc abc", 1).offsetInLine()).isEqualTo(4);
	}

	@Test
	void storeUnknownCharacters()
	{
		assertDiagnostics("\u2412\u4123\u1234",
			assertedDiagnostic(0, 0, 0, 1, LexerError.UNKNOWN_CHARACTER),
			assertedDiagnostic(1, 1, 0, 1, LexerError.UNKNOWN_CHARACTER),
			assertedDiagnostic(2, 2, 0, 1, LexerError.UNKNOWN_CHARACTER)
		);
	}

	@Test
	void storeUnknownCharactersAfterTokens()
	{
		assertDiagnostics("WRITE #var\n\u2412\u4123\u1234",
			assertedDiagnostic(11, 0, 1, 1, LexerError.UNKNOWN_CHARACTER),
			assertedDiagnostic(12, 1, 1, 1, LexerError.UNKNOWN_CHARACTER),
			assertedDiagnostic(13, 2, 1, 1, LexerError.UNKNOWN_CHARACTER)
		);
	}

	@Test
	void storeTheCorrectOffsetInLine()
	{
		var source = """
			DEFINE DATA
			LOCAL
			1 #INLDA (A2)
			END-DEFINE
			""";

		var tokens = lexSource(source);
		assertThat(tokens.advanceAfterNext(SyntaxKind.NUMBER)).isTrue();
		var identifier = tokens.peek();
		assertThat(identifier.line()).isEqualTo(2);
		assertThat(identifier.offsetInLine()).isEqualTo(2);
	}

}
