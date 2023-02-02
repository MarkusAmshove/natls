package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IDecideForConditionBranchNode;
import org.amshove.natparse.natural.conditionals.IConditionNode;

class DecideForConditionBranchNode extends StatementWithBodyNode implements IDecideForConditionBranchNode
{
	private IConditionNode criteria;

	@Override
	public IConditionNode criteria()
	{
		return criteria;
	}

	void setCriteria(IConditionNode criteria)
	{
		addNode((BaseSyntaxNode) criteria);
		this.criteria = criteria;
	}
}
