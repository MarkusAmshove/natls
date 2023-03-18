package org.amshove.natparse.natural;

public interface IAssignmentStatementNode extends IStatementNode
{
	IOperandNode target();

	IOperandNode operand();
}
