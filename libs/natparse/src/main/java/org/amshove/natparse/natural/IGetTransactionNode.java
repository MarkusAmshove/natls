package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface IGetTransactionNode extends IStatementNode
{
	ReadOnlyList<IOperandNode> operands();
}
