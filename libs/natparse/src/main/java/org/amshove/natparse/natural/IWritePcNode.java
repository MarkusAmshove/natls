package org.amshove.natparse.natural;

public interface IWritePcNode extends IStatementNode
{
	ILiteralNode number();

	boolean isVariable();

	IOperandNode operand();
}
