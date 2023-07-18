package org.amshove.natparse.natural;

public interface IAssignmentStatementNode extends IMutateVariables
{
	IOperandNode target();

	IOperandNode operand();
}
