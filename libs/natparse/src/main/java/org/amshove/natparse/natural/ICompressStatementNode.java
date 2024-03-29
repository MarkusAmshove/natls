package org.amshove.natparse.natural;

import java.util.List;

public interface ICompressStatementNode extends IMutateVariables
{
	IOperandNode intoTarget();

	List<IOperandNode> operands();

	boolean isNumeric();

	boolean isFull();

	boolean isLeavingSpace();

	boolean isWithDelimiters();

	boolean isWithAllDelimiters();

	IOperandNode delimiter();
}
