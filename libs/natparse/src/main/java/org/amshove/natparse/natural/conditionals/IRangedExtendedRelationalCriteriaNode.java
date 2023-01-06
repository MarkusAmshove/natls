package org.amshove.natparse.natural.conditionals;

import org.amshove.natparse.natural.IOperandNode;

import java.util.Optional;

public non-sealed interface IRangedExtendedRelationalCriteriaNode extends ILogicalConditionCriteriaNode
{
	IOperandNode left();

	default ComparisonOperator operator()
	{
		return ComparisonOperator.EQUAL;
	}

	IOperandNode lowerBound();

	IOperandNode upperBound();

	Optional<IOperandNode> excludedLowerBound();

	Optional<IOperandNode> excludedUpperBound();
}
