package org.amshove.natparse.natural;

import javax.annotation.Nullable;

public interface IReduceDynamicNode extends IStatementNode
{
	IVariableReferenceNode variableToReduce();

	IOperandNode sizeToReduceTo();

	@Nullable
	IVariableReferenceNode errorVariable();
}
