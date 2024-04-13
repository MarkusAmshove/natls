package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface IEndTransactionNode extends IStatementNode
{
	ReadOnlyList<IOperandNode> operands();
}
