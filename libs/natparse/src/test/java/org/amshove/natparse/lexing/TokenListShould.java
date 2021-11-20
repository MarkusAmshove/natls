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
		var token = SyntaxTokenFactory.create(SyntaxKind.ADD, 0, 0, 1, "ADD");
		var list = createTokenList(token);
		assertThat(list.peekWithInsignificant()).isEqualTo(token);
	}

	@Test
	void beAbleToAdvance()
	{
		var token = SyntaxTokenFactory.create(SyntaxKind.ADD, 0, 0, 1, "ADD");
		var list = createTokenList(
			SyntaxTokenFactory.create(SyntaxKind.ASSIGN, 0, 0, 1, "ASSIGN"),
			token
		);
		assertThat(list.peekWithInsignificant()).isNotEqualTo(token);
		list.advance();
		assertThat(list.peekWithInsignificant()).isEqualTo(token);
	}

	@Test
	void returnNullWhenPeekingOverSize()
	{
		var tokenList = createListWithTokens(10);
		assertThat(tokenList.peekWithInsignificant()).isNotNull();
		assertThat(tokenList.peekWithInsignificant(10)).isNull();
	}

	@Test
	void recognizeWhenAtEnd()
	{
		var tokenList = createListWithTokens(2);
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

		var syntaxKinds = SyntaxKind.values();
		for (var i = 0; i < amountOfTokens; i++)
		{
			tokens.add(SyntaxTokenFactory.create(syntaxKinds[i], 0, 0, 0, syntaxKinds[i].toString()));
		}

		return TokenList.fromTokens(tokens);
	}
}
