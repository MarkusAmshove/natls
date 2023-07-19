package org.amshove.natparse.natural;

import javax.annotation.Nullable;

public interface IResizeDynamicNode extends IMutateVariables
{
	IVariableReferenceNode variableToResize();

	IOperandNode sizeToResizeTo();

	@Nullable
	IVariableReferenceNode errorVariable();
}
