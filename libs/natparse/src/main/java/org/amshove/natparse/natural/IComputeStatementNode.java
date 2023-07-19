package org.amshove.natparse.natural;

public interface IComputeStatementNode extends IMutateVariables
{
	IOperandNode target();

	IOperandNode operand();

	boolean isRounded();
}
