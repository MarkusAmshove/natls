package org.amshove.natparse.lexing;

import org.junit.jupiter.api.Test;

import java.util.List;

public class LexerForBuiltinFunctionsShould extends AbstractLexerTest
{
	@Test
	void lexTimX()
	{
		assertTokens("*TIMX", token(SyntaxKind.TIMX, "*TIMX"));
	}

	@Test
	void lexDatX()
	{
		assertTokens("*DATX", token(SyntaxKind.DATX, "*DATX"));
	}
}
