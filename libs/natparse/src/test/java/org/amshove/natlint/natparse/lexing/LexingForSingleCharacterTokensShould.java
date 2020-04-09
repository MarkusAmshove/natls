package org.amshove.natlint.natparse.lexing;

import org.junit.jupiter.api.Test;

public class LexingForSingleCharacterTokensShould extends AbstractLexerTest
{
	@Test
	void lexLeftParenthesis()
	{
		assertTokens("(", token(SyntaxKind.LPAREN, "("));
	}

	@Test
	void lexRightParenthesis()
	{
		assertTokens(")", token(SyntaxKind.RPAREN, ")"));
	}

	@Test
	void lexEqualsSign()
	{
		assertTokens("=", token(SyntaxKind.EQUALS, "="));
	}

	@Test
	void lexColon()
	{
		assertTokens(":", token(SyntaxKind.COLON, ":"));
	}

	@Test
	void lexPlus()
	{
		assertTokens("+", token(SyntaxKind.PLUS, "+"));
	}

	@Test
	void lexMinus()
	{
		assertTokens("-", token(SyntaxKind.MINUS, "-"));
	}

	@Test
	void lexAsterisk()
	{
		assertTokens(" *", token(SyntaxKind.WHITESPACE), token(SyntaxKind.ASTERISK, "*"));
	}

	@Test
	void lexSlash()
	{
		assertTokens("/", token(SyntaxKind.SLASH, "/"));
	}

	@Test
	void lexBackslash()
	{
		assertTokens("\\", token(SyntaxKind.BACKSLASH, "\\"));
	}

	@Test
	void lexSemicolon()
	{
		assertTokens(";", token(SyntaxKind.SEMICOLON, ";"));
	}

	@Test
	void lexGreater()
	{
		assertTokens(">", token(SyntaxKind.GREATER, ">"));
	}

	@Test
	void lexLesser()
	{
		assertTokens("<", token(SyntaxKind.LESSER, "<"));
	}
}
