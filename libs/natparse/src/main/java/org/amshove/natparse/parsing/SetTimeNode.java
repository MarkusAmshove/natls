package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.ISetTimeNode;

class SetTimeNode extends StatementNode implements ISetTimeNode
{
	@Override
	public SyntaxToken labelIdentifier()
	{
		return null;
	}
}
