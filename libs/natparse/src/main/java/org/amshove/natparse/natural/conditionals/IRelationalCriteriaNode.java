package org.amshove.natparse.natural.conditionals;

import org.amshove.natparse.natural.IOperandNode;

public non-sealed interface IRelationalCriteriaNode extends ILogicalConditionCriteriaNode, IHasComparisonOperator
{
	IOperandNode left();
	ComparisonOperator operator();
	IOperandNode right();
}
