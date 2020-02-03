package org.amshove.natlint.natparse.linting;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class LexerShould
{
	@TestFactory
	Iterable<DynamicTest> lintWhitespaceTokens()
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
					.as("Expected [%s] but was [%s]", expectedToken.escapedExpectedSource(), actualToken.escapedSource())
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
