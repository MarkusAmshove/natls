package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface ICallLoopNode extends IStatementWithBodyNode, IMutateVariables
{
	IOperandNode calling();

	ReadOnlyList<IOperandNode> operands();
}
