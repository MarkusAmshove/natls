package org.amshove.natparse.lexing;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractLexerTest
{
	protected SyntaxToken lexSingle(String source, int index)
	{
		var tokenList = new Lexer().lex(source, Paths.get("TEST.NSN"));
		for (var i = 0; i < index; i++)
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
		var lexemes = lexSource(source);
		assertThat(lexemes.diagnostics()).as("Expected the source to lex without diagnostics").isEmpty();

		var allTokens = lexemes.allTokens().stream().map(t -> t.kind().toString()).collect(Collectors.joining(", "));
		var allTokensMessage = "All tokens: (%s)".formatted(allTokens);

		for (var i = 0; i < expectedTokens.size(); i++)
		{
			var expectedToken = expectedTokens.get(i);
			var actualToken = lexemes.peek();

			assertThat(actualToken.kind())
				.as(
					"Expected Token %d to be [%s] but was [%s]: '%s'. %s",
					i + 1,
					expectedToken.kind,
					actualToken.kind(),
					actualToken.source(),
					allTokensMessage
				)
				.isEqualTo(expectedToken.kind);

			if (expectedToken.source != null)
			{
				assertThat(actualToken.source())
					.as("Expected source [%s] but was [%s]. %s", expectedToken.source(), actualToken.source(), allTokensMessage)
					.isEqualTo(expectedToken.source);
			}

			lexemes.advance();
		}
	}

	protected TokenList lexSource(String source)
	{
		return new Lexer().lex(source, Paths.get("TEST.NSN"));
	}

	protected TokenList assertDiagnostic(String source, LexerDiagnostic diagnostic)
	{
		return assertDiagnostics(source, diagnostic);
	}

	protected TokenList assertDiagnostics(String source, LexerDiagnostic... diagnostics)
	{
		var lexer = new Lexer();
		var lexemes = lexer.lex(source, Paths.get("TEST.NSN"));
		var foundDiagnostics = lexemes.diagnostics();

		for (var diagnostic : diagnostics)
		{
			assertThat(foundDiagnostics)
				.as("Expected lex result to contain diagnostic [%s]", diagnostic)
				.contains(diagnostic);
		}

		return lexemes;
	}

	protected void assertTokensInOrder(TokenList tokenList, SyntaxKind... kinds)
	{
		var nonWhitespaceTokens = tokenList
			.allTokens()
			.stream()
			.toList();

		assertThat(nonWhitespaceTokens.size())
			.as("Token count mismatch. Expected [%d] but got [%d]", kinds.length, tokenList.size())
			.isEqualTo(kinds.length);

		for (var i = 0; i < kinds.length; i++)
		{
			assertThat(nonWhitespaceTokens.get(i).kind()).isEqualTo(kinds[i]);
		}
	}

	protected LexerDiagnostic assertedDiagnostic(int offset, int offsetInLine, int currentLine, int length, LexerError error)
	{
		return LexerDiagnostic.create(offset, offsetInLine, currentLine, length, Paths.get("LexerTest.NSN"), error);
	}

	protected record ExpectedSyntaxToken(SyntaxKind kind, String source)
	{}
}
