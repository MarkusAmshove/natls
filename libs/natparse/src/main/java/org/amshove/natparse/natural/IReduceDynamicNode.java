package org.amshove.natparse.natural;

import javax.annotation.Nullable;

public interface IReduceDynamicNode extends IMutateVariables
{
	IVariableReferenceNode variableToReduce();

	IOperandNode sizeToReduceTo();

	@Nullable
	IVariableReferenceNode errorVariable();
}
