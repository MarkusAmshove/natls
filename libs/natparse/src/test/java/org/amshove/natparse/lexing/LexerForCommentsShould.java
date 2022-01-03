package org.amshove.natparse.lexing;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class LexerForCommentsShould extends AbstractLexerTest
{
	@Test
	void lexSingleAsteriskComments()
	{
		var tokenList = lexSource("* Hello from comment");
		assertThat(tokenList.size()).isEqualTo(0);
		assertThat(tokenList.comments().size()).isEqualTo(1);
		assertThat(tokenList.comments().get(0).kind()).isEqualTo(SyntaxKind.COMMENT);
	}

	@Test
	void lexSingleAsteriskCommentsSpecialCase()
	{
		// This is special because it is not allowed according to the Natural documentation,
		// but NaturalONE does allow it
		var tokenList = lexSource("*/ Hello from comment");
		assertThat(tokenList.size()).isEqualTo(0);
		assertThat(tokenList.comments().size()).isEqualTo(1);
		assertThat(tokenList.comments().get(0).kind()).isEqualTo(SyntaxKind.COMMENT);
	}

	@Test
	void lexSingleAsteriskCommentsWithDoubleAsterisk()
	{
		var tokenList = lexSource("**Hello from comment");
		assertThat(tokenList.size()).isEqualTo(0);
		assertThat(tokenList.comments().size()).isEqualTo(1);
		assertThat(tokenList.comments().get(0).kind()).isEqualTo(SyntaxKind.COMMENT);
	}

	@Test
	void lexEmptySingleAsteriskCommentsAtFileEnd()
	{
		var tokenList = lexSource("\n*");
		assertThat(tokenList.size()).isEqualTo(0);
		assertThat(tokenList.comments().size()).isEqualTo(1);
		assertThat(tokenList.comments().get(0).kind()).isEqualTo(SyntaxKind.COMMENT);
	}

	@Test
	void lexEmptySingleAsteriskCommentsAtLineEnd()
	{
		var tokenList = lexSource("\n*\n");
		assertThat(tokenList.size()).isEqualTo(0);
		assertThat(tokenList.comments().size()).isEqualTo(1);
		assertThat(tokenList.comments().get(0).kind()).isEqualTo(SyntaxKind.COMMENT);
	}

	@Test
	void lexInlineComment()
	{
		var tokenList = lexSource("   /* Inline comment!");
		assertThat(tokenList.size()).isEqualTo(0);
		assertThat(tokenList.comments().size()).isEqualTo(1);
		assertThat(tokenList.comments().get(0).kind()).isEqualTo(SyntaxKind.COMMENT);
		assertThat(tokenList.comments().get(0).source()).isEqualTo("/* Inline comment!");
	}

	@Test
	void lexInlineCommentBeforeLinebreak()
	{
		var tokenList = lexSource("GT\n/*INCOMMENT\nGT");
		assertThat(tokenList.size()).isEqualTo(2);
		assertThat(tokenList.comments().size()).isEqualTo(1);
		assertThat(tokenList.comments().get(0).kind()).isEqualTo(SyntaxKind.COMMENT);
		assertThat(tokenList.comments().get(0).source()).isEqualTo("/*INCOMMENT");

		assertThat(tokenList.peek().kind()).isEqualTo(SyntaxKind.GT);
		assertThat(tokenList.peek(1).kind()).isEqualTo(SyntaxKind.GT);
	}
}
