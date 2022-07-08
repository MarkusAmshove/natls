package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IDescriptorNode;

class DescriptorNode extends TokenNode implements IDescriptorNode
{
	public DescriptorNode(SyntaxToken token)
	{
		super(token);
	}
}
