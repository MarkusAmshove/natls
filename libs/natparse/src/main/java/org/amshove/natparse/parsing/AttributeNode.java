package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.IAttributeNode;
import org.amshove.natparse.natural.IDataType;

class AttributeNode extends TokenNode implements IAttributeNode
{
	private static final IDataType DATA_TYPE = new LiteralNode.LiteralType(DataFormat.CONTROL, 2);

	public AttributeNode(SyntaxToken token)
	{
		super(token);
	}

	@Override
	public IDataType dataType()
	{
		return DATA_TYPE;
	}
}
