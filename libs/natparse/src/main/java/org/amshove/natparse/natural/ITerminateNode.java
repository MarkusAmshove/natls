package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface ITerminateNode extends IStatementNode
{
	ReadOnlyList<IOperandNode> operands();
}
