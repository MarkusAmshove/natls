package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IValueAttributeNode;

class ValueAttributeNode extends BaseSyntaxNode implements IValueAttributeNode
{
	private final SyntaxKind kind;
	private final String value;

	ValueAttributeNode(SyntaxToken token)
	{
		// TODO: Make this more safe. Does every Attribute have an equals?
		addNode(new TokenNode(token));
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
