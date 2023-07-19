package org.amshove.natparse.natural;

import java.util.List;

public interface ISortStatementNode extends IStatementWithBodyNode, IMutateVariables
{
	List<SortedOperand> operands();

	List<IOperandNode> usings();
}
