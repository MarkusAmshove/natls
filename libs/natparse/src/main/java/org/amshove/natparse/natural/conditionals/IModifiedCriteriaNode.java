package org.amshove.natparse.natural.conditionals;

import org.amshove.natparse.natural.IOperandNode;

public non-sealed interface IModifiedCriteriaNode extends ILogicalConditionCriteriaNode
{
	IOperandNode operand();

	boolean isNotModified();
}
