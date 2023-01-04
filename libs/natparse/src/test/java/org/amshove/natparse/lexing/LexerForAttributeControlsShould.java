package org.amshove.natparse.lexing;

import org.junit.jupiter.api.Test;

public class LexerForAttributeControlsShould extends AbstractLexerTest
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
}
