package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.conditionals.IUnaryLogicalCriteriaNode;

class UnaryLogicalCriteriaNode extends BaseSyntaxNode implements IUnaryLogicalCriteriaNode
{
	private ISyntaxNode node;

	@Override
	public ISyntaxNode node()
	{
		return node;
	}

	void setNode(ISyntaxNode node)
	{
		addNode(((BaseSyntaxNode) node));
		this.node = node;
	}
}
