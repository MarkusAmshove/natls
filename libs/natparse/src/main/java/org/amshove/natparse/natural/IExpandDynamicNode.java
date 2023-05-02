package org.amshove.natparse.natural;

import javax.annotation.Nullable;

public interface IExpandDynamicNode extends IStatementNode
{
	IVariableReferenceNode variableToExpand();

	int sizeToExpandTo();

	@Nullable
	IVariableReferenceNode errorVariable();
}
