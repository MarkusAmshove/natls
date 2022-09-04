package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.ILabelIdentifierNode;

class LabelIdentifierNode extends BaseSyntaxNode implements ILabelIdentifierNode
{
	private SyntaxToken label;

	@Override
	public SyntaxToken label()
	{
		return label;
	}

	void setLabel(SyntaxToken label)
	{
		this.label = label;
	}
}
