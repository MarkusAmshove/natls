package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.conditionals.ILogicalConditionCriteriaNode;
import org.amshove.natparse.natural.conditionals.INegatedConditionalCriteria;

class NegatedConditionalCriteria extends BaseSyntaxNode implements INegatedConditionalCriteria
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
