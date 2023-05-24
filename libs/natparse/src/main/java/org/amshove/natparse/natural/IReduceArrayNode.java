package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

import javax.annotation.Nullable;

public interface IReduceArrayNode extends IStatementNode
{
	IVariableReferenceNode arrayToReduce();

	@Nullable
	IVariableReferenceNode errorVariable();

	ReadOnlyList<IOperandNode> dimensions();
}
