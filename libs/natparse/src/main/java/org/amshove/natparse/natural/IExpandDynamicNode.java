package org.amshove.natparse.natural;

public interface IExpandDynamicNode extends IStatementNode
{
	IVariableReferenceNode variableToExpand();

	int sizeToExpandTo();
}
