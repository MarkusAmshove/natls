package org.amshove.natparse.lexing;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractLexerTest
{
	protected SyntaxToken lexSingle(String source, int index)
	{
		TokenList tokenList = new Lexer().lex(source);
		for(var i = 0; i < index; i++)
		{
			tokenList.advance();
		}
		return tokenList.peek();
	}

	protected SyntaxToken lexSingle(String source)
	{
		return lexSingle(source, 0);
	}

	protected ExpectedSyntaxToken token(SyntaxKind expectedKind)
	{
		return new ExpectedSyntaxToken(expectedKind, null);
	}

	protected ExpectedSyntaxToken token(SyntaxKind expectedKind, String expectedSource)
	{
		return new ExpectedSyntaxToken(expectedKind, expectedSource);
	}

	protected void assertTokens(String source, SyntaxKind... expectedKinds)
	{
		assertTokens(source, Arrays.stream(expectedKinds).map(this::token).collect(Collectors.toList()));
	}

	protected void assertTokens(String source, ExpectedSyntaxToken... expectedTokens)
	{
		assertTokens(source, Arrays.asList(expectedTokens));
	}

	protected void assertTokens(String source, List<ExpectedSyntaxToken> expectedTokens)
	{
		var lexer = new Lexer();
		var lexemes = lexer.lex(source);
		for (var i = 0; i < expectedTokens.size(); i++)
		{
			var expectedToken = expectedTokens.get(i);
			var actualToken = lexemes.peek();

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

			lexemes.advance();
		}
	}

	protected record ExpectedSyntaxToken(SyntaxKind kind, String expectedSource)
	{
		private String escapedExpectedSource()
		{
			if (kind != SyntaxKind.NEW_LINE && kind != SyntaxKind.TAB)
			{
				return expectedSource;
			}
			return expectedSource.replace("\r", "\\r").replace("\n", "\\n").replace("\t", "\\t");
		}

	}
}
