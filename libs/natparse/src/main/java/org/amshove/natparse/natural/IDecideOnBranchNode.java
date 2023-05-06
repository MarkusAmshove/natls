package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface IDecideOnBranchNode
{
	ReadOnlyList<IOperandNode> values();

	IStatementListNode body();
}
