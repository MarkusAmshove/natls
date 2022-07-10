package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.ILiteralNode;

class LiteralNode extends TokenNode implements ILiteralNode
{
	public LiteralNode(SyntaxToken token)
	{
		super(token);
	}
}
