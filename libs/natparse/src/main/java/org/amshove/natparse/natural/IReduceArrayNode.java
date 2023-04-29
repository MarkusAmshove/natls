package org.amshove.natparse.natural;

import javax.annotation.Nullable;

public interface IReduceArrayNode extends IStatementNode
{
	IVariableReferenceNode arrayToReduce();

	@Nullable
	IVariableReferenceNode errorVariable();
}
