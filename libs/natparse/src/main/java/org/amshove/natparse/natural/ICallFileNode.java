package org.amshove.natparse.natural;

public interface ICallFileNode extends IStatementWithBodyNode
{
	ILiteralNode calling();

	IOperandNode controlField();

	IOperandNode recordArea();
}
