package org.amshove.natparse.natural;

public interface IResizeDynamicNode extends IStatementNode
{
	IVariableReferenceNode variableToResize();
	int sizeToResizeTo();
}
