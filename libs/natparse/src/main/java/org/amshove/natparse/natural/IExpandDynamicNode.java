package org.amshove.natparse.natural;

import org.jspecify.annotations.Nullable;

public interface IExpandDynamicNode extends IMutateVariables
{
	IVariableReferenceNode variableToExpand();

	IOperandNode sizeToExpandTo();

	@Nullable
	IVariableReferenceNode errorVariable();
}
