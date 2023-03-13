package org.amshove.natparse.natural;

public interface IAssignStatementNode extends IStatementNode
{
	IOperandNode target();

	IOperandNode operand();

	boolean isRounded();
}
