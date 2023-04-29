package org.amshove.natparse.natural;

import javax.annotation.Nullable;

public interface IResizeDynamicNode extends IStatementNode
{
	IVariableReferenceNode variableToResize();

	int sizeToResizeTo();

	@Nullable
	IVariableReferenceNode errorVariable();
}
