package org.amshove.natparse.lexing;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class TokenListShould
{
	@Test
	void returnTheTokenAtCurrentOffset()
	{
		SyntaxToken token = SyntaxTokenFactory.create(SyntaxKind.ADD, 0, 0, 1, "ADD");
		var list = createTokenList(token);
		assertThat(list.peek()).isEqualTo(token);
	}

	@Test
	void beAbleToAdvance()
	{
		SyntaxToken token = SyntaxTokenFactory.create(SyntaxKind.ADD, 0, 0, 1, "ADD");
		var list = createTokenList(
			SyntaxTokenFactory.create(SyntaxKind.ASSIGN, 0, 0, 1, "ASSIGN"),
			token
		);
		assertThat(list.peek()).isNotEqualTo(token);
		list.advance();
		assertThat(list.peek()).isEqualTo(token);
	}

	@Test
	void returnNullWhenPeekingOverSize()
	{
		TokenList tokenList = createListWithTokens(10);
		assertThat(tokenList.peek()).isNotNull();
		assertThat(tokenList.peek(10)).isNull();
	}

	@Test
	void recognizeWhenAtEnd()
	{
		TokenList tokenList = createListWithTokens(2);
		assertThat(tokenList.isAtEnd()).isFalse();
		tokenList.advance();
		assertThat(tokenList.isAtEnd()).isFalse();
		tokenList.advance();
		assertThat(tokenList.isAtEnd()).isTrue();
	}

	private TokenList createTokenList(SyntaxToken... tokens)
	{
		return TokenList.fromTokens(Arrays.stream(tokens).toList());
	}

	private TokenList createListWithTokens(int amountOfTokens)
	{
		var tokens = new ArrayList<SyntaxToken>(amountOfTokens);

		SyntaxKind[] syntaxKinds = SyntaxKind.values();
		for (var i = 0; i < amountOfTokens; i++)
		{
			tokens.add(SyntaxTokenFactory.create(syntaxKinds[i], 0, 0, 0, syntaxKinds[i].toString()));
		}

		return TokenList.fromTokens(tokens);
	}
}
