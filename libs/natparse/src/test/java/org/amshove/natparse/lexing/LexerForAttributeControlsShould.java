package org.amshove.natparse.lexing;

import org.junit.jupiter.api.Test;

class LexerForAttributeControlsShould extends AbstractLexerTest
{
	@Test
	void consumeEverythingBelongingToAnEditorMask()
	{
		assertTokens(
			"(EM=YYYY-MM-DD)",
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.EM, "EM=YYYY-MM-DD"),
			token(SyntaxKind.RPAREN)
		);
	}

	@Test
	void recognizeEmWhenFollowingAnIdentifier()
	{
		assertTokens(
			"#FIRST-VAR-LEVEL.SECOND-LEVEL(EM=YY-MM-DD)",
			token(SyntaxKind.IDENTIFIER, "#FIRST-VAR-LEVEL.SECOND-LEVEL"),
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.EM),
			token(SyntaxKind.RPAREN)
		);
	}

	@Test
	void consumeAttributeDefinition()
	{
		assertTokens(
			"#PAGE(AD=MI)",
			token(SyntaxKind.IDENTIFIER, "#PAGE"),
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.AD),
			token(SyntaxKind.RPAREN)
		);
	}

	@Test
	void consumeAttributeDefinitionWithFillerCharacter()
	{
		assertTokens(
			"(AD=ODL'_')",
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.AD, "AD=ODL'_'"),
			token(SyntaxKind.RPAREN)
		);
	}

	@Test
	void consumeAttributeDefinitionWithWhitespaceAsFillerCharacter()
	{
		assertTokens(
			"(AD=ODL' ')",
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.AD, "AD=ODL' '"),
			token(SyntaxKind.RPAREN)
		);
	}

	@Test
	void consumeColorDefinition()
	{
		assertTokens(
			"MOVE (CD=RE)",
			token(SyntaxKind.MOVE),
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.CD),
			token(SyntaxKind.RPAREN)
		);
	}

	@Test
	void consumeDynamicAttributes()
	{
		assertTokens(
			"(DY=<U>)",
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.DY, "DY=<U>"),
			token(SyntaxKind.RPAREN)
		);
	}

	@Test
	void consumeDynamicAttributesInCombinationWithOtherAttributes()
	{
		assertTokens(
			"(DY=<U> CD=RE)",
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.DY, "DY=<U>"),
			token(SyntaxKind.CD, "CD=RE"),
			token(SyntaxKind.RPAREN)
		);
	}

	@Test
	void consumeComplexDynamicAttributes()
	{
		assertTokens(
			"(DY='27YEPD'28GRPD'29TUPD'30)",
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.DY, "DY='27YEPD'28GRPD'29TUPD'30"),
			token(SyntaxKind.RPAREN)
		);
	}

	@Test
	void consumeNL()
	{
		assertTokens(
			"(NL=12,7)",
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.NL, "NL=12,7"),
			token(SyntaxKind.RPAREN)
		);
	}

	@Test
	void consumeAL()
	{
		assertTokens(
			"(AL=20)",
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.AL, "AL=20"),
			token(SyntaxKind.RPAREN)
		);
	}

	@Test
	void consumeDF()
	{
		assertTokens(
			"(DF=S)",
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.DF, "DF=S"),
			token(SyntaxKind.RPAREN)
		);
	}

	@Test
	void consumeIP()
	{
		assertTokens(
			"(IP=OFF)",
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.IP, "IP=OFF"),
			token(SyntaxKind.RPAREN)
		);
	}

	@Test
	void consumeIS()
	{
		assertTokens(
			"(IS=OFF)",
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.IS, "IS=OFF"),
			token(SyntaxKind.RPAREN)
		);
	}

	@Test
	void consumeCV()
	{
		assertTokens(
			"(CV=#VAR)",
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.CV, "CV="),
			token(SyntaxKind.IDENTIFIER, "#VAR"),
			token(SyntaxKind.RPAREN)
		);
	}
}
