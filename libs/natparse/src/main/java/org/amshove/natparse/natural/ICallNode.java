package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface ICallNode extends IStatementNode
{
	IOperandNode calling();

	ReadOnlyList<IOperandNode> operands();
}
