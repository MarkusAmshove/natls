package org.amshove.natparse.natural;

import java.util.List;
import java.util.Map;

public interface ISortStatementNode extends IStatementWithBodyNode
{
	Map<IOperandNode, SortDirection> operands();

	List<IOperandNode> usings();

}
