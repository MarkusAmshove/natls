package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IConstantAttributeNode;

class ConstantAttributeNode extends BaseSyntaxNode implements IConstantAttributeNode
{
	private final SyntaxKind kind;

	ConstantAttributeNode(SyntaxToken token)
	{
		addNode(new TokenNode(token));
		kind = token.kind();
	}

	@Override
	public SyntaxKind kind()
	{
		return kind;
	}

}
