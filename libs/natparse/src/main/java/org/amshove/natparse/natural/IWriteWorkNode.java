package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface IWriteWorkNode extends IStatementNode
{
	ILiteralNode number();

	boolean isVariable();

	ReadOnlyList<IOperandNode> operands();
}
