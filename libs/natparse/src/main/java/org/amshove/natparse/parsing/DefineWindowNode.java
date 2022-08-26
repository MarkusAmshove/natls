package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IDefineWindowNode;

class DefineWindowNode extends StatementNode implements IDefineWindowNode
{
	private SyntaxToken name;

	@Override
	public SyntaxToken name()
	{
		return name;
	}

	void setName(SyntaxToken name)
	{
		this.name = name;
	}
}
