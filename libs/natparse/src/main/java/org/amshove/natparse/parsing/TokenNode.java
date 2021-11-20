package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.ITokenNode;

public class TokenNode extends BaseSyntaxNode implements ITokenNode
{
	private final SyntaxToken token;

	public TokenNode(SyntaxToken token)
	{
		this.token = token;
		setStart(token);
		setEnd(token);
	}

	@Override
	public SyntaxToken token()
	{
		return token;
	}
}
