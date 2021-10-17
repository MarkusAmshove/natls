package org.amshove.natparse.lexing;

import java.util.List;

public class TokenList
{
	private List<SyntaxToken> tokens;
	private int currentOffset = 0;

	TokenList(List<SyntaxToken> tokens)
	{
		this.tokens = tokens;
	}

	public static TokenList fromTokens(List<SyntaxToken> tokenList)
	{
		return new TokenList(tokenList);
	}

	public SyntaxToken peek()
	{
		return peek(0);
	}

	public SyntaxToken peek(int offset)
	{
		int index = currentOffset + offset;
		if(isAtEnd(index))
		{
			return null;
		}
		return tokens.get(index);
	}

	public void advance()
	{
		currentOffset++;
	}

	public boolean isAtEnd()
	{
		return isAtEnd(currentOffset);
	}

	private boolean isAtEnd(int offset)
	{
		return offset >= tokens.size();
	}
}
