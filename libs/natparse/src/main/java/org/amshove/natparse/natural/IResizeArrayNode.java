package org.amshove.natparse.natural;

import javax.annotation.Nullable;

public interface IResizeArrayNode extends IStatementNode
{
	IVariableReferenceNode arrayToResize();

	@Nullable
	IVariableReferenceNode errorVariable();
}
