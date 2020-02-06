package org.amshove.natlint.natparse.linting;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@Nested
public class LexerShould
{
	@Nested
	class LexWhitespaceTokens
	{
		@TestFactory
		Iterable<DynamicTest> lexWhitespaceTokens()
		{
			return Arrays.asList(
				dynamicTest("Single Whitespace", () -> assertTokens(" ", token(SyntaxKind.WHITESPACE, " "))),
				dynamicTest("Multiple Whitespace", () -> assertTokens("  ", token(SyntaxKind.WHITESPACE, "  "))),
				dynamicTest("Single Tab", () -> assertTokens("\t", token(SyntaxKind.TAB, "\t"))),
				dynamicTest("Multiple Tabs", () -> assertTokens("\t\t\t", token(SyntaxKind.TAB, "\t\t\t"))),
				dynamicTest("Mixed Whitespace and Tab",
					() -> assertTokens(
						" \t  \t  ",
						SyntaxKind.WHITESPACE,
						SyntaxKind.TAB,
						SyntaxKind.WHITESPACE,
						SyntaxKind.TAB,
						SyntaxKind.WHITESPACE)),
				dynamicTest("Unix Newline", () -> assertTokens("\n", token(SyntaxKind.NEW_LINE, "\n"))),
				dynamicTest("Windows Newline", () -> assertTokens("\r\n", token(SyntaxKind.NEW_LINE, "\r\n"))),
				dynamicTest("Mixed Newline",
					() -> assertTokens(
						"\n\r\n\n",
						SyntaxKind.NEW_LINE,
						SyntaxKind.NEW_LINE,
						SyntaxKind.NEW_LINE)));
		}
	}

	@Nested
	class StoreTokenProperties
	{
		@Test
		void storeTheLineInformationOfTokens()
		{
			Lexer lexer = new Lexer();
			List<SyntaxToken> tokens = lexer.lex("abc!\nabc$");

			SyntaxToken firstAbc = tokens.get(0);
			assertThat(firstAbc.line()).isEqualTo(0);

			SyntaxToken lineEnd = tokens.get(1);
			assertThat(lineEnd.line()).isEqualTo(0);

			SyntaxToken secondAbc = tokens.get(2);
			assertThat(secondAbc.line()).isEqualTo(1);
		}

		@ParameterizedTest
		@CsvSource(
		{ "abc!$,3", "bbbbcda!$,7" })
		void storeTheLengthOfTokens(String source, int expectedLength)
		{
			SyntaxToken token = lexSingle(source);
			assertThat(token.length())
				.as("Expected token with length [%d] but was [%d]. Actual token: [%s]", expectedLength, token.length(), token.toString())
				.isEqualTo(expectedLength);
		}

		@ParameterizedTest
		@CsvSource(
		{ "abc!$,0,0", "abc! cba!$,2,5" })
		void storeTheOffsetOfTokens(String source, int nthIndex, int expectedOffset)
		{
			SyntaxToken token = lexSingle(source, nthIndex);
			assertThat(token.offset())
				.as("Expected Token at index [%d] but was [%d]", expectedOffset, token.offset())
				.isEqualTo(expectedOffset);
		}

		@Test
		void storeTheOffsetInLineOfTokens()
		{
			assertThat(lexSingle("abc! abc!$", 2).offsetInLine()).isEqualTo(5);
		}
	}

	@Nested
	class LexSingleCharacterTokens
	{
		@TestFactory
		Iterable<DynamicTest> lexSingleCharacterTokens()
		{
			return Arrays.asList(
				dynamicTest("Left parenthesis", () -> assertTokens("(", token(SyntaxKind.LPAREN, "("))),
				dynamicTest("Right parenthesis", () -> assertTokens(")", token(SyntaxKind.RPAREN, ")"))),
				dynamicTest("Equals sign", () -> assertTokens("=", token(SyntaxKind.EQUALS, "="))),
				dynamicTest("Colon", () -> assertTokens(":", token(SyntaxKind.COLON, ":"))),
				dynamicTest("Plus", () -> assertTokens("+", token(SyntaxKind.PLUS, "+"))),
				dynamicTest("Minus", () -> assertTokens("-", token(SyntaxKind.MINUS, "-"))),
				dynamicTest("Asterisk", () -> assertTokens("*", token(SyntaxKind.ASTERISK, "*"))),
				dynamicTest("Slash", () -> assertTokens("/", token(SyntaxKind.SLASH, "/"))),
				dynamicTest("Backslash", () -> assertTokens("\\", token(SyntaxKind.BACKSLASH, "\\"))),
				dynamicTest("Semicolon", () -> assertTokens(";", token(SyntaxKind.SEMICOLON, ";"))),
				dynamicTest("Greater", () -> assertTokens(">", token(SyntaxKind.GREATER, ">"))),
				dynamicTest("Lesser", () -> assertTokens("<", token(SyntaxKind.LESSER, "<"))));
		}
	}

	private SyntaxToken lexSingle(String source, int index)
	{
		return new Lexer().lex(source).get(index);
	}

	private SyntaxToken lexSingle(String source)
	{
		return lexSingle(source, 0);
	}

	private ExpectedSyntaxToken token(SyntaxKind expectedKind)
	{
		return new ExpectedSyntaxToken(expectedKind, null);
	}

	private ExpectedSyntaxToken token(SyntaxKind expectedKind, String expectedSource)
	{
		return new ExpectedSyntaxToken(expectedKind, expectedSource);
	}

	void assertTokens(String source, SyntaxKind... expectedKinds)
	{
		assertTokens(source, Arrays.stream(expectedKinds).map(this::token).collect(Collectors.toList()));
	}

	void assertTokens(String source, ExpectedSyntaxToken... expectedTokens)
	{
		assertTokens(source, Arrays.asList(expectedTokens));
	}

	void assertTokens(String source, List<ExpectedSyntaxToken> expectedTokens)
	{
		Lexer lexer = new Lexer();
		List<SyntaxToken> lexemes = lexer.lex(source);
		for (int i = 0; i < expectedTokens.size(); i++)
		{
			ExpectedSyntaxToken expectedToken = expectedTokens.get(i);
			SyntaxToken actualToken = lexemes.get(i);

			assertThat(actualToken.kind())
				.as("Expected Token %d to be [%s] but was [%s]: '%s'",
					i + 1,
					expectedToken.kind,
					actualToken.kind(),
					actualToken.escapedSource())
				.isEqualTo(expectedToken.kind);

			if (expectedToken.expectedSource != null)
			{
				assertThat(actualToken.source())
					.as("Expected source [%s] but was [%s]", expectedToken.escapedExpectedSource(), actualToken.escapedSource())
					.isEqualTo(expectedToken.expectedSource);
			}
		}
	}

	private static class ExpectedSyntaxToken
	{
		private final SyntaxKind kind;
		private final String expectedSource;

		private String escapedExpectedSource()
		{
			if (kind != SyntaxKind.NEW_LINE && kind != SyntaxKind.TAB)
			{
				return expectedSource;
			}
			return expectedSource.replace("\r", "\\r").replace("\n", "\\n").replace("\t", "\\t");
		}

		private ExpectedSyntaxToken(SyntaxKind kind, String expectedSource)
		{
			this.kind = kind;
			this.expectedSource = expectedSource;
		}
	}
}
