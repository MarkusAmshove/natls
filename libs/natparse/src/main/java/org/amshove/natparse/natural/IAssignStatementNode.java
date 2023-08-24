package org.amshove.natparse.natural;

public interface IAssignStatementNode extends IMutateVariables
{
	IOperandNode target();

	IOperandNode operand();

	boolean isRounded();
}
