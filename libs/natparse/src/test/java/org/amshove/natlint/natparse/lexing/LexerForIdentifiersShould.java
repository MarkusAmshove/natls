package org.amshove.natlint.natparse.lexing;

import org.junit.jupiter.api.Test;

public class LexerForIdentifiersShould extends AbstractLexerTest
{
	@Test
	void recognizeIdentifiersStartingWithHash()
	{
		assertTokens("#NAME", token(SyntaxKind.IDENTIFIER, "#NAME"));
	}

	@Test
	void recognizeAivVariables()
	{
		assertTokens("+MY-AIV", token(SyntaxKind.IDENTIFIER, "+MY-AIV"));
	}
}
