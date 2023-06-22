package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.SortDirection;
import org.amshove.natparse.natural.ISortStatementNode;
import org.amshove.natparse.natural.IOperandNode;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class SortStatementNode extends StatementWithBodyNode implements ISortStatementNode
{
	private static final Map<IOperandNode, SortDirection> operands = new HashMap<>();
	private final List<IOperandNode> usings = new ArrayList<>();

	@Override
	public Map<IOperandNode, SortDirection> operands()
	{
		return operands;
	}

	@Override
	public List<IOperandNode> usings()
	{
		return usings;
	}

	void addSortBy(IOperandNode operand, SortDirection direction)
	{
		operands.put(operand, direction);
	}

	void addUsing(IOperandNode using)
	{
		usings.add(using);
	}
}
