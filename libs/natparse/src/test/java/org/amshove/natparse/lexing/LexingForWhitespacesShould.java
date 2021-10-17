package org.amshove.natparse.lexing;

import org.junit.jupiter.api.Test;

public class LexingForWhitespacesShould extends AbstractLexerTest
{

	@Test
	void recognizeSingleWhitespace()
	{
		assertTokens(" ", token(SyntaxKind.WHITESPACE, " "));
	}

	@Test
	void recognizeMultipleWhitespace()
	{
		assertTokens("  ", token(SyntaxKind.WHITESPACE, "  "));
	}

	@Test
	void recognizeSingleTabs()
	{
		assertTokens("\t", token(SyntaxKind.TAB, "\t"));
	}

	@Test
	void recognizeMultipleTabs()
	{
		assertTokens("\t\t\t", token(SyntaxKind.TAB, "\t\t\t"));
	}

	@Test
	void recognizeMixedWhitespaceAndTabs()
	{
		assertTokens(
			" \t  \t  ",
			SyntaxKind.WHITESPACE,
			SyntaxKind.TAB,
			SyntaxKind.WHITESPACE,
			SyntaxKind.TAB,
			SyntaxKind.WHITESPACE);
	}

	@Test
	void recognizeWindowsNewLines()
	{
		assertTokens("\r\n", token(SyntaxKind.NEW_LINE, "\r\n"));
	}

	@Test
	void recognizeUnixNewLines()
	{
		assertTokens("\n", token(SyntaxKind.NEW_LINE, "\n"));
	}

	@Test
	void recognizeMixedNewLines()
	{
		assertTokens(
			"\n\r\n\n",
			SyntaxKind.NEW_LINE,
			SyntaxKind.NEW_LINE,
			SyntaxKind.NEW_LINE);
	}
}
