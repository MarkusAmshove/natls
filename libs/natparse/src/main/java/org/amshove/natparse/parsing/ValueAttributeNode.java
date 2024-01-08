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
		addNode(new TokenNode(token));
		var splitByEqual = token.source().split("=");
		kind = SyntaxKind.valueOf(splitByEqual[0].toUpperCase());
		value = splitByEqual[1];
	}

	ValueAttributeNode(SyntaxToken token, SyntaxToken nextToken)
	{
		addNode(new TokenNode(token));
		var sourceWithoutEquals = token.source().replace("=", "");
		kind = SyntaxKind.valueOf(sourceWithoutEquals.toUpperCase());
		value = nextToken.source();
	}

	/**
	 * Used for implicit attributes like (I)
	 */
	ValueAttributeNode(SyntaxKind kind, SyntaxToken valueToken)
	{
		addNode(new TokenNode(valueToken));
		this.value = valueToken.source();
		this.kind = kind;
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
