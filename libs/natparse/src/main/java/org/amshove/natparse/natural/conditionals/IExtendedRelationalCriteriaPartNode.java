package org.amshove.natparse.natural.conditionals;

import org.amshove.natparse.natural.IOperandNode;

public interface IExtendedRelationalCriteriaPartNode extends IHasComparisonOperator
{
	IOperandNode rhs();

	default ComparisonOperator operator()
	{
		return ComparisonOperator.EQUAL;
	}
}
