package org.amshove.natparse.natural;

public interface IAssignmentStatementNode extends IStatementNode, IMutateVariables
{
	IOperandNode target();

	IOperandNode operand();
}
