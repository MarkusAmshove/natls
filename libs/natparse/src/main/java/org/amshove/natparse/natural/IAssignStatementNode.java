package org.amshove.natparse.natural;

public interface IAssignStatementNode extends IStatementNode, IMutateVariables
{
	IOperandNode target();

	IOperandNode operand();

	boolean isRounded();
}
