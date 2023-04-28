package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.ISetWindowNode;

class SetWindowNode extends StatementNode implements ISetWindowNode
{
	private SyntaxToken token;

	@Override
	public SyntaxToken window()
	{
		return token;
	}

	void setWindow(SyntaxToken node)
	{
		token = node;
	}
}
