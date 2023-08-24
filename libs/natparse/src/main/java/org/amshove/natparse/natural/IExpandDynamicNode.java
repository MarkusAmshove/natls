package org.amshove.natparse.natural;

import javax.annotation.Nullable;

public interface IExpandDynamicNode extends IMutateVariables
{
	IVariableReferenceNode variableToExpand();

	IOperandNode sizeToExpandTo();

	@Nullable
	IVariableReferenceNode errorVariable();
}
