package org.amshove.natparse.natural.conditionals;

public non-sealed interface IChainedCriteriaNode extends ILogicalConditionCriteriaNode
{
	ILogicalConditionCriteriaNode left();

	ChainedCriteriaOperator operator();

	ILogicalConditionCriteriaNode right();
}
