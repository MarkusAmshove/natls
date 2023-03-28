package org.amshove.natparse.natural;

public non-sealed interface IDivideStatementNode extends IBasicMathStatementNode
{
	IOperandNode giving();

	IOperandNode remainder();

	boolean hasRemainder();
}
