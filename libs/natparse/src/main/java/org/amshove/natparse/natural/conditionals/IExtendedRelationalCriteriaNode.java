package org.amshove.natparse.natural.conditionals;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IOperandNode;

public non-sealed interface IExtendedRelationalCriteriaNode extends ILogicalConditionCriteriaNode, IHasComparisonOperator
{
	IOperandNode left();

	default ComparisonOperator operator()
	{
		return ComparisonOperator.EQUAL;
	}

	ReadOnlyList<IOperandNode> rights();
}
