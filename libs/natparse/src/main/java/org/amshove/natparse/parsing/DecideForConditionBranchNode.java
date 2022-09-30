package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IDecideForConditionBranchNode;
import org.amshove.natparse.natural.conditionals.ILogicalConditionCriteriaNode;

class DecideForConditionBranchNode extends StatementWithBodyNode implements IDecideForConditionBranchNode
{
	private ILogicalConditionCriteriaNode criteria;

	@Override
	public ILogicalConditionCriteriaNode criteria()
	{
		return criteria;
	}

	void setCriteria(ILogicalConditionCriteriaNode criteria)
	{
		addNode((BaseSyntaxNode) criteria);
		this.criteria = criteria;
	}
}
