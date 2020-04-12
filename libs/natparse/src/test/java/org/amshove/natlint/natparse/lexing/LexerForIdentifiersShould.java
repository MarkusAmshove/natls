package org.amshove.natlint.natparse.lexing;

import org.junit.jupiter.api.Test;

public class LexerForIdentifiersShould extends AbstractLexerTest
{
	@Test
	void recognizeIdentifiersStartingWithHash()
	{
		assertTokens("#NAME", token(SyntaxKind.IDENTIFIER, "#NAME"));
	}
}
