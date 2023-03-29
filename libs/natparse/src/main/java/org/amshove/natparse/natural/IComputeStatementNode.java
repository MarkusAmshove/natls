package org.amshove.natparse.natural;

public interface IComputeStatementNode extends IStatementNode, IMutateVariables
{
	IOperandNode target();

	IOperandNode operand();

	boolean isRounded();
}
