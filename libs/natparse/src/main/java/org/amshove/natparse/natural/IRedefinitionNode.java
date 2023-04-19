package org.amshove.natparse.natural;

public interface IRedefinitionNode extends IGroupNode, ISymbolReferenceNode
{
	IVariableNode target();

	int fillerBytes();
}
