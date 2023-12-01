package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IAttributeNode;

class AttributeNode extends TokenNode implements IAttributeNode
{
	private final SyntaxKind kind;
	private final String value;

	AttributeNode(SyntaxToken token)
	{
		super(token);

		// TODO: Make this more safe. Does every Attribute have an equals?
		var splitByEqual = token.source().split("=");
		kind = SyntaxKind.valueOf(splitByEqual[0].toUpperCase());
		value = splitByEqual[1];
	}

	@Override
	public SyntaxKind kind()
	{
		return kind;
	}

	@Override
	public String value()
	{
		return value;
	}
}
