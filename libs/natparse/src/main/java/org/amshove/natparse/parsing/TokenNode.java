package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.ITokenNode;

public class TokenNode extends BaseSyntaxNode implements ITokenNode
{
	private final SyntaxToken token;

	public TokenNode(SyntaxToken token)
	{
		this.token = token;
	}

	@Override
	public SyntaxToken token()
	{
		return token;
	}

	@Override
	public int offset()
	{
		return token.offset();
	}

	@Override
	public int offsetInLine()
	{
		return token.offsetInLine();
	}

	@Override
	public int line()
	{
		return token.line();
	}

	@Override
	public int length()
	{
		return token.length();
	}

	@Override public String toString()
	{
		return "TokenNode{token=%s}".formatted(token);
	}
}
