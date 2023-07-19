package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface IInputStatementNode extends IMutateVariables
{
	ReadOnlyList<IOperandNode> operands();
}
