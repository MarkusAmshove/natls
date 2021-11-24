package org.amshove.natparse.natural;

public interface IVariableNode extends ISymbolNode
{
	String name();
	String qualifiedName();
	int level();
	DataFormat dataFormat();
	double dataLength();
	VariableScope scope();
}
