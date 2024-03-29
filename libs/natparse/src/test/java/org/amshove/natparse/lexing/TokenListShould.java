package org.amshove.natparse.lexing;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class TokenListShould
{
	private SyntaxToken createToken(SyntaxKind kind, int offset, int offsetInLine, int line, String source)
	{
		return SyntaxTokenFactory.create(kind, offset, offsetInLine, line, source, Paths.get("TESTMODULE.NSN"));
	}

	@Test
	void returnTheTokenAtCurrentOffset()
	{
		var token = createToken(SyntaxKind.ADD, 0, 0, 1, "ADD");
		var list = createTokenList(token);
		assertThat(list.peek()).isEqualTo(token);
	}

	@Test
	void beAbleToAdvance()
	{
		var token = createToken(SyntaxKind.ADD, 0, 0, 1, "ADD");
		var list = createTokenList(
			createToken(SyntaxKind.ASSIGN, 0, 0, 1, "ASSIGN"),
			token
		);
		assertThat(list.peek()).isNotEqualTo(token);
		list.advance();
		assertThat(list.peek()).isEqualTo(token);
	}

	@Test
	void returnNullWhenPeekingOverSize()
	{
		var tokenList = createListWithTokens(10);
		assertThat(tokenList.peek()).isNotNull();
		assertThat(tokenList.peek(10)).isNull();
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

	@Test
	void advanceOverWhitespace()
	{
		var tokenList = createTokenList(
			SyntaxKind.ADD,
			SyntaxKind.IDENTIFIER
		);

		assertThat(tokenList.peek().kind()).isEqualTo(SyntaxKind.ADD);
		tokenList.advance();
		assertThat(tokenList.peek().kind()).isEqualTo(SyntaxKind.IDENTIFIER);
	}

	@Test
	void consumeAToken()
	{
		var tokenList = createTokenList(
			SyntaxKind.LOCAL,
			SyntaxKind.USING,
			SyntaxKind.IDENTIFIER
		);

		assertThat(tokenList.peek().kind()).isEqualTo(SyntaxKind.LOCAL);
		assertThat(tokenList.consume(SyntaxKind.LOCAL)).isTrue();
		assertThat(tokenList.peek().kind()).isEqualTo(SyntaxKind.USING);
	}

	@Test
	void returnNullOnPeekingBelowIndexZero()
	{
		var tokenList = createTokenList(SyntaxKind.LOCAL);

		assertThat(tokenList.peek(-1)).isNull();
	}

	@Test
	void returnTrueOnPeekKindsIfTheOrderMatches()
	{
		var tokenList = createTokenList(SyntaxKind.LOCAL, SyntaxKind.USING, SyntaxKind.IDENTIFIER);

		assertThat(tokenList.peekKinds(SyntaxKind.LOCAL, SyntaxKind.USING, SyntaxKind.IDENTIFIER)).isTrue();
	}

	@Test
	void returnFalseOnPeekKindsIfTheOrderDoesntMatch()
	{
		var tokenList = createTokenList(SyntaxKind.LOCAL, SyntaxKind.USING, SyntaxKind.IDENTIFIER);

		assertThat(tokenList.peekKinds(SyntaxKind.GLOBAL, SyntaxKind.USING, SyntaxKind.IDENTIFIER)).isFalse();
	}

	@Test
	void returnFalseOnPeekKindsIfTheLengthOfPeeksIsGreaterThanTheRemainingLength()
	{
		var tokenList = createTokenList(SyntaxKind.LOCAL, SyntaxKind.USING, SyntaxKind.IDENTIFIER);

		tokenList.advance();

		assertThat(tokenList.peekKinds(SyntaxKind.USING, SyntaxKind.IDENTIFIER, SyntaxKind.LOCAL)).isFalse();
	}

	private TokenList createTokenList(SyntaxToken... tokens)
	{
		return TokenList.fromTokens(Paths.get("TOKENLISTSHOULD.NSN"), Arrays.stream(tokens).toList());
	}

	private TokenList createTokenList(SyntaxKind... tokenKinds)
	{
		return TokenList.fromTokens(Paths.get("TOKENLISTSHOULD.NSN"), Arrays.stream(tokenKinds).map(k -> createToken(k, 0, 0, 0, "")).toList());
	}

	private TokenList createListWithTokens(int amountOfTokens)
	{
		var tokens = new ArrayList<SyntaxToken>(amountOfTokens);

		var syntaxKinds = SyntaxKind.values();
		for (var i = 0; i < amountOfTokens; i++)
		{
			tokens.add(createToken(syntaxKinds[i], 0, 0, 0, syntaxKinds[i].toString()));
		}

		return TokenList.fromTokens(Paths.get("TOKENLISTSHOULD.NSN"), tokens);
	}
}
