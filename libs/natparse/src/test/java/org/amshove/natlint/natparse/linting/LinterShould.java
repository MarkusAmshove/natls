package org.amshove.natlint.natparse.linting;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LinterShould
{
	@Test
	void lintWhitespaceTokens()
	{
		assertTokens(" ", token(SyntaxKind.WHITESPACE, " "));
	}

	private ExpectedSyntaxToken token(SyntaxKind expectedKind, String expectedSource)
	{
		return new ExpectedSyntaxToken(expectedKind, expectedSource);
	}

	//https://joel-costigliola.github.io/assertj/assertj-core-custom-assertions.html
	void assertTokens(String source, ExpectedSyntaxToken... expectedTokens)
	{
		Lexer lexer = new Lexer();
		List<SyntaxToken> lexemes = lexer.lex(source);
		for (int i = 0; i < expectedTokens.length; i++)
		{
			ExpectedSyntaxToken expectedToken = expectedTokens[i];
			SyntaxToken actualToken = lexemes.get(i);

			assertThat(actualToken.getKind()).isEqualTo(expectedToken.kind);
			assertThat(actualToken.getSource()).isEqualTo(expectedToken.expectedSource);
		}
	}

	private class ExpectedSyntaxToken
	{
		private final SyntaxKind kind;
		private final String expectedSource;

		private ExpectedSyntaxToken(SyntaxKind kind, String expectedSource)
		{
			this.kind = kind;
			this.expectedSource = expectedSource;
		}
	}
}
