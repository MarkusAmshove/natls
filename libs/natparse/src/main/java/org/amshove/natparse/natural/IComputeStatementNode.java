package org.amshove.natparse.natural;

public interface IComputeStatementNode extends IStatementNode
{
	IOperandNode target();
	IOperandNode operand();
	boolean isRounded();
}
