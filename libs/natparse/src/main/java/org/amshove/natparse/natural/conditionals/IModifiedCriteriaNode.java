package org.amshove.natparse.natural.conditionals;

import org.amshove.natparse.natural.IVariableReferenceNode;

public non-sealed interface IModifiedCriteriaNode extends ILogicalConditionCriteriaNode
{
	IVariableReferenceNode variable();
	boolean isNotModified();
}
