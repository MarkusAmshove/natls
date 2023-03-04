package org.amshove.natparse.natural.conditionals;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IOperandNode;

public non-sealed interface IExtendedRelationalCriteriaNode extends ILogicalConditionCriteriaNode
{
	IOperandNode left();

	ReadOnlyList<IExtendedRelationalCriteriaPartNode> rights();
}
