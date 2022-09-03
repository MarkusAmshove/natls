package org.amshove.natparse.natural.conditionals;

import org.amshove.natparse.natural.IOperandNode;

public non-sealed interface IRelationalExpressionCriteriaNode extends ILogicalConditionCriteriaNode
{
	IOperandNode left();
	ComparisonOperator operator();
	IOperandNode right();
}
