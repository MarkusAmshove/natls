package org.amshove.natparse.natural;

import org.jspecify.annotations.Nullable;

public interface IReduceDynamicNode extends IMutateVariables
{
	IVariableReferenceNode variableToReduce();

	IOperandNode sizeToReduceTo();

	@Nullable
	IVariableReferenceNode errorVariable();
}
