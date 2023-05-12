package org.amshove.natparse.lexing;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class LexerForAttributeControlsShould extends AbstractLexerTest
{
	@Test
	void recognizeAttributes()
	{
		assertThat(SyntaxKind.AD.isAttribute());
		assertThat(SyntaxKind.DY.isAttribute());
		assertThat(SyntaxKind.CD.isAttribute());
		assertThat(SyntaxKind.EM.isAttribute());
		assertThat(SyntaxKind.NL.isAttribute());
		assertThat(SyntaxKind.AL.isAttribute());
		assertThat(SyntaxKind.DF.isAttribute());
		assertThat(SyntaxKind.IP.isAttribute());
		assertThat(SyntaxKind.IS.isAttribute());
		assertThat(SyntaxKind.CV.isAttribute());
		assertThat(SyntaxKind.ZP.isAttribute());
		assertThat(SyntaxKind.SG.isAttribute());
		assertThat(SyntaxKind.ES.isAttribute());
		assertThat(SyntaxKind.SB.isAttribute());
	}

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
	void consumeZP()
	{
		assertTokens(
			"(ZP=OFF)",
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.ZP, "ZP=OFF"),
			token(SyntaxKind.RPAREN)
		);
	}

	@Test
	void consumeSP()
	{
		assertTokens(
			"(SG=OFF)",
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.SG, "SG=OFF"),
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

	@Test
	void consumeSBWithALiteral()
	{
		assertTokens(
			"(SB='literal', #VAR1, #VAR2)",
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.SB, "SB="),
			token(SyntaxKind.STRING_LITERAL, "'literal'"),
			token(SyntaxKind.COMMA, ","),
			token(SyntaxKind.IDENTIFIER, "#VAR1"),
			token(SyntaxKind.COMMA, ","),
			token(SyntaxKind.IDENTIFIER, "#VAR2"),
			token(SyntaxKind.RPAREN)
		);
	}

	@Test
	void consumeSBWithArray()
	{
		assertTokens(
			"(SB=#ARR(*))",
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.SB, "SB="),
			token(SyntaxKind.IDENTIFIER, "#ARR"),
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.ASTERISK),
			token(SyntaxKind.RPAREN),
			token(SyntaxKind.RPAREN)
		);
	}

	@Test
	void consumeES()
	{
		assertTokens(
			"(ES=ON)",
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.ES, "ES=ON"),
			token(SyntaxKind.RPAREN)
		);
	}
}
