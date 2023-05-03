package org.amshove.natparse.lexing;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class LexerForDatatypesShould extends AbstractLexerTest
{
	@Test
	void recognizeSimpleDataTypes()
	{
		assertTokens("A10", SyntaxKind.IDENTIFIER);
	}

	@Test
	void recognizeArrays()
	{
		assertTokens("A10/*", SyntaxKind.IDENTIFIER, SyntaxKind.SLASH, SyntaxKind.ASTERISK);
	}

	@Test
	void recognizeArraysWithWhitespace()
	{
		assertTokens("A10/ 1: 10", SyntaxKind.IDENTIFIER, SyntaxKind.SLASH, SyntaxKind.NUMBER_LITERAL, SyntaxKind.COLON, SyntaxKind.NUMBER_LITERAL);
	}

	@Override
	protected void assertTokens(String source, SyntaxKind... expectedKinds)
	{
		var tokens = lexSource("""
			DEFINE DATA LOCAL
			1 #VAR (%s)
			END-DEFINE
			""".formatted(source));

		while (tokens.peek().kind() != SyntaxKind.LPAREN)
		{
			tokens.advance();
		}
		tokens.advance();

		for (var expectedKind : expectedKinds)
		{
			var actualToken = tokens.advance();
			assertThat(actualToken.kind())
				.as("Tokens from here to end: " + tokens.subrange(tokens.getCurrentOffset(), tokens.size() - 1).stream().map(SyntaxToken::kind).map(Enum::name).collect(Collectors.joining(", ")))
				.isEqualTo(expectedKind);
		}

		assertThat(tokens.peek().kind())
			.as("Expected data type end token (RPAREN)")
			.isEqualTo(SyntaxKind.RPAREN);
	}
}
