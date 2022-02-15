package org.amshove.natparse.lexing;

import org.junit.jupiter.api.Test;

public class LexerForAttributeControlsShould extends AbstractLexerTest
{
	@Test
	void consumeEverythingBelongingToAnEditorMask()
	{
		assertTokens("(EM=YYYY-MM-DD)",
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.EDITOR_MASK, "EM=YYYY-MM-DD"),
			token(SyntaxKind.RPAREN)
		);
	}

	@Test
	void recognizeEmWhenFollowingAnIdentifier()
	{
		assertTokens("#FIRST-VAR-LEVEL.SECOND-LEVEL(EM=YY-MM-DD)",
			token(SyntaxKind.IDENTIFIER, "#FIRST-VAR-LEVEL.SECOND-LEVEL"),
			token(SyntaxKind.LPAREN),
			token(SyntaxKind.EDITOR_MASK),
			token(SyntaxKind.RPAREN)
		);
	}
}
