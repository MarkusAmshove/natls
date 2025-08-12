package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;
import org.jspecify.annotations.Nullable;

public interface IResizeArrayNode extends IMutateVariables
{
	IVariableReferenceNode arrayToResize();

	ReadOnlyList<IOperandNode> dimensions();

	@Nullable
	IVariableReferenceNode errorVariable();
}
