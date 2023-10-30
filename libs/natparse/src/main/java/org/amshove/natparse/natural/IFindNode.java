package org.amshove.natparse.natural;

public interface IFindNode extends IStatementWithBodyNode, IAdabasAccessStatementNode
{
	IVariableReferenceNode view();
}
