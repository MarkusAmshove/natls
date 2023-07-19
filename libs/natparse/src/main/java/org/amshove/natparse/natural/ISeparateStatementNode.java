package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface ISeparateStatementNode extends IMutateVariables
{
	IOperandNode separated();

	ReadOnlyList<IOperandNode> targets();
}
