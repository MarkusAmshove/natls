package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface IGetTransactionNode extends IMutateVariables
{
	ReadOnlyList<IOperandNode> operands();
}
