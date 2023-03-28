package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public sealed interface IBasicMathStatementNode extends IStatementNode permits IAddStatementNode,IDivideStatementNode,IMultiplyGivingStatementNode,IMultiplyStatementNode,ISubtractGivingStatementNode,ISubtractStatementNode
{
	IOperandNode target();

	boolean isRounded();

	boolean isGiving();

	ReadOnlyList<IOperandNode> operands();
}
