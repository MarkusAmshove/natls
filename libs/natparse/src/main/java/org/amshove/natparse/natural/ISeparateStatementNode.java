package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface ISeparateStatementNode extends IStatementNode
{
	IOperandNode separated();

	ReadOnlyList<IOperandNode> intoList();
}
