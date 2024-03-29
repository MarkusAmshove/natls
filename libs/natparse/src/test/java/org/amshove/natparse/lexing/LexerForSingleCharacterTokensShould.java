package org.amshove.natparse.lexing;

import org.junit.jupiter.api.Test;

public class LexerForSingleCharacterTokensShould extends AbstractLexerTest
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
		assertTokens("=", token(SyntaxKind.EQUALS_SIGN, "="));
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
		assertTokens(" *", token(SyntaxKind.ASTERISK, "*"));
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
		assertTokens(">", token(SyntaxKind.GREATER_SIGN, ">"));
	}

	@Test
	void lexLesser()
	{
		assertTokens("<", token(SyntaxKind.LESSER_SIGN, "<"));
	}

	@Test
	void lexDot()
	{
		assertTokens(".", token(SyntaxKind.DOT, "."));
	}

	@Test
	void lexComma()
	{
		assertTokens(",", token(SyntaxKind.COMMA, ","));
	}

	@Test
	void lexLeftBracket()
	{
		assertTokens("[", token(SyntaxKind.LBRACKET, "["));
	}

	@Test
	void lexRightBracket()
	{
		assertTokens("]", token(SyntaxKind.RBRACKET, "]"));
	}

	@Test
	void lexCaret()
	{
		assertTokens("^", token(SyntaxKind.CARET, "^"));
	}

	@Test
	void lexExclamationMark()
	{
		assertTokens("!", token(SyntaxKind.COMMA, "!"));
		assertTokens("!!", token(SyntaxKind.SQL_CONCAT, "!!"));
	}

	@Test
	void lexPercent()
	{
		assertTokens("%", token(SyntaxKind.PERCENT, "%"));
	}

	@Test
	void lexQuestionmark()
	{
		assertTokens("?", token(SyntaxKind.QUESTIONMARK, "?"));
	}

	@Test
	void lexUnderscore()
	{
		assertTokens("_", token(SyntaxKind.UNDERSCORE, "_"));
	}

	@Test
	void lexSectionSymbol()
	{
		assertTokens("\u00A7", token(SyntaxKind.SECTION_SYMBOL, "\u00A7"));
	}
}
