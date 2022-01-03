package org.amshove.natparse.lexing;

import org.junit.jupiter.api.Test;

public class LexerForSystemVariablesShould extends AbstractLexerTest
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

	@Test
	void lexDatN()
	{
		assertTokens("*DATN", token(SyntaxKind.DATN, "*DATN"));
	}

	@Test
	void lexLanguage()
	{
		assertTokens("*LANGUAGE", token(SyntaxKind.LANGUAGE, "*LANGUAGE"));
	}

	@Test
	void lexProgram()
	{
		assertTokens("*PROGRAM", token(SyntaxKind.PROGRAM, "*PROGRAM"));
	}

	@Test
	void lexUser()
	{
		assertTokens("*USER", token(SyntaxKind.USER, "*USER"));
	}

	@Test
	void lexLibraryId()
	{
		assertTokens("*LIBRARY-ID", token(SyntaxKind.LIBRARY_ID, "*LIBRARY-ID"));
	}
}
