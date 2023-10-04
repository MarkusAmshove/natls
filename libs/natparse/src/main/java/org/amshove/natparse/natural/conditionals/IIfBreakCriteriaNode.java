package org.amshove.natparse.natural.conditionals;

import org.amshove.natparse.natural.IOperandNode;

public non-sealed interface IIfBreakCriteriaNode extends ILogicalConditionCriteriaNode
{
	IOperandNode operand();
}
