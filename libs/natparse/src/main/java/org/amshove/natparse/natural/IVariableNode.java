package org.amshove.natparse.natural;

public interface IVariableNode extends ISymbolNode
{
	VariableScope scope();
	int level();
	String name();
	String qualifiedName();
}
