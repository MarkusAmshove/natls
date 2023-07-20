package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface IResetStatementNode extends IMutateVariables
{
	ReadOnlyList<IOperandNode> operands();
}
