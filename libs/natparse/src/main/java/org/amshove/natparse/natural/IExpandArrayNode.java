package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

import javax.annotation.Nullable;

public interface IExpandArrayNode extends IMutateVariables
{
	IVariableReferenceNode arrayToExpand();

	ReadOnlyList<IOperandNode> dimensions();

	@Nullable
	IVariableReferenceNode errorVariable();
}
