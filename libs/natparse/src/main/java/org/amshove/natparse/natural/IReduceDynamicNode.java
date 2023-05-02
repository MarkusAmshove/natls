package org.amshove.natparse.natural;

import javax.annotation.Nullable;

public interface IReduceDynamicNode extends IStatementNode
{
	IVariableReferenceNode variableToReduce();

	int sizeToReduceTo();

	@Nullable
	IVariableReferenceNode errorVariable();
}
