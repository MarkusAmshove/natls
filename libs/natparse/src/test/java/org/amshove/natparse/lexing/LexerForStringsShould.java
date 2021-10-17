package org.amshove.natparse.lexing;

import org.junit.jupiter.api.Test;

public class LexerForStringsShould extends AbstractLexerTest
{
	@Test
	void lexSingleQuoteStrings()
	{
		assertTokens("'Hello World!'", token(SyntaxKind.STRING, "'Hello World!'"));
	}

	@Test
	void lexDoubleQuoteStrings()
	{
		assertTokens("\"Hello World!\"", token(SyntaxKind.STRING, "\"Hello World!\""));
	}
}
