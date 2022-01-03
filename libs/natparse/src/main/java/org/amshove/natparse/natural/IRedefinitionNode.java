package org.amshove.natparse.natural;

public interface IRedefinitionNode extends IGroupNode
{
	IVariableNode target();
	int fillerBytes();
}
