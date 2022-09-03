package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.conditionals.IConditionNode;
import org.amshove.natparse.natural.conditionals.ILogicalConditionCriteriaNode;

class ConditionNode extends BaseSyntaxNode implements IConditionNode
{
	private ILogicalConditionCriteriaNode criteria;

	@Override
	public ILogicalConditionCriteriaNode criteria()
	{
		return criteria;
	}

	void setCriteria(ILogicalConditionCriteriaNode criteria)
	{
		addNode((BaseSyntaxNode)criteria);
		this.criteria = criteria;
	}
}
