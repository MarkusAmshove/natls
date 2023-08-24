package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface IMutateVariables extends IStatementNode
{
	ReadOnlyList<IOperandNode> mutations();
}
