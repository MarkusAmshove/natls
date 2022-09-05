package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.conditionals.ChainedCriteriaOperator;
import org.amshove.natparse.natural.conditionals.IChainedCriteriaNode;
import org.amshove.natparse.natural.conditionals.ILogicalConditionCriteriaNode;

class ChainedCriteriaNode extends BaseSyntaxNode implements IChainedCriteriaNode
{
	private ILogicalConditionCriteriaNode left;
	private ChainedCriteriaOperator operator;
	private ILogicalConditionCriteriaNode right;

	@Override
	public ILogicalConditionCriteriaNode left()
	{
		return left;
	}

	@Override
	public ChainedCriteriaOperator operator()
	{
		return operator;
	}

	@Override
	public ILogicalConditionCriteriaNode right()
	{
		return right;
	}

	void setLeft(ILogicalConditionCriteriaNode left)
	{
		addNode(((BaseSyntaxNode) left));
		this.left = left;
	}

	void setRight(ILogicalConditionCriteriaNode right)
	{
		addNode(((BaseSyntaxNode) right));
		this.right = right;
	}

	void setOperator(ChainedCriteriaOperator operator)
	{
		this.operator = operator;
	}
}
