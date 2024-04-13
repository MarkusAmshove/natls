package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IEndNode;

class EndNode extends StatementNode implements IEndNode
{
	private SyntaxToken endToken;

	@Override
	public SyntaxToken token()
	{
		return endToken;
	}

	void setEndToken(SyntaxToken endToken)
	{
		this.endToken = endToken;
	}
}
