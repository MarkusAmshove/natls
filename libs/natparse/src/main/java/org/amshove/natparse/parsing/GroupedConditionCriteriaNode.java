package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.conditionals.IGroupedConditionCriteria;
import org.amshove.natparse.natural.conditionals.ILogicalConditionCriteriaNode;

class GroupedConditionCriteriaNode extends BaseSyntaxNode implements IGroupedConditionCriteria
{
	private ILogicalConditionCriteriaNode criteria;

	@Override
	public ILogicalConditionCriteriaNode criteria()
	{
		return criteria;
	}

	void setCriteria(ILogicalConditionCriteriaNode criteria)
	{
		addNode(((BaseSyntaxNode) criteria));
		this.criteria = criteria;
	}
}
