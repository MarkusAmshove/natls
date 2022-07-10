package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IAttributeNode;

class AttributeNode extends TokenNode implements IAttributeNode
{
	public AttributeNode(SyntaxToken token)
	{
		super(token);
	}
}
