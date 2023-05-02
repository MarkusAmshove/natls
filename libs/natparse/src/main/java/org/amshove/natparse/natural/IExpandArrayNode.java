package org.amshove.natparse.natural;

import javax.annotation.Nullable;

public interface IExpandArrayNode extends IStatementNode
{
	IVariableReferenceNode arrayToExpand();

	@Nullable
	IVariableReferenceNode errorVariable();
}
