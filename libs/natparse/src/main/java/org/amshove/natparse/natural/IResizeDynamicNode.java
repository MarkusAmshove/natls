package org.amshove.natparse.natural;

import org.jspecify.annotations.Nullable;

public interface IResizeDynamicNode extends IMutateVariables
{
	IVariableReferenceNode variableToResize();

	IOperandNode sizeToResizeTo();

	@Nullable
	IVariableReferenceNode errorVariable();
}
