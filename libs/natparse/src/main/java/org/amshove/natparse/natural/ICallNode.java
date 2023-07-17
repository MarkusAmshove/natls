package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface ICallNode extends IMutateVariables
{
	IOperandNode calling();

	ReadOnlyList<IOperandNode> operands();
}
