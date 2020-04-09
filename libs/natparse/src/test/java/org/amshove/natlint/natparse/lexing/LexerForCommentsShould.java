package org.amshove.natlint.natparse.lexing;

import org.amshove.natlint.natparse.lexing.text.SourceTextScanner;
import org.junit.jupiter.api.Test;

public class LexerForCommentsShould extends AbstractLexerTest
{
	@Test
	void lexSingleAsteriskComments()
	{
		assertTokens("* Hello from comment", token(SyntaxKind.COMMENT, "* Hello from comment"));
	}

	@Test
	void lexInlineComment()
	{
		assertTokens("   /* Inline comment!", token(SyntaxKind.WHITESPACE), token(SyntaxKind.COMMENT, "/* Inline comment!"));
	}

	@Test
	void lexInlineCommentBeforeLinebreak()
	{
		assertTokens("GT\n/*INCOMMENT\nGT" + SourceTextScanner.END_CHARACTER,
			token(SyntaxKind.GT),
			token(SyntaxKind.NEW_LINE),
			token(SyntaxKind.COMMENT, "/*INCOMMENT"),
			token(SyntaxKind.NEW_LINE),
			token(SyntaxKind.GT));
	}
}
