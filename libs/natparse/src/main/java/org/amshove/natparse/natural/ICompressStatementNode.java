package org.amshove.natparse.natural;

import java.util.List;

public interface ICompressStatementNode extends IStatementNode
{
	IOperandNode intoTarget();

	List<IOperandNode> operands();

	boolean isNumeric();

	boolean isFull();

	boolean isLeavingSpace();

	boolean isWithDelimiters();
}
