package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.ISortStatementNode;
import org.amshove.natparse.natural.SortedOperand;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IOperandNode;

import java.util.List;
import java.util.ArrayList;

class SortStatementNode extends StatementWithBodyNode implements ISortStatementNode
{
	private static final List<SortedOperand> operands = new ArrayList<>();
	private final List<IOperandNode> usings = new ArrayList<>();

	@Override
	public List<SortedOperand> operands()
	{
		return operands;
	}

	@Override
	public List<IOperandNode> usings()
	{
		return usings;
	}

	void addSortBy(SortedOperand operand)
	{
		operands.add(operand);
	}

	void addUsing(IOperandNode using)
	{
		usings.add(using);
	}

	@Override
	public ReadOnlyList<IOperandNode> mutations()
	{
		var mutations = new ArrayList<IOperandNode>();

		for (SortedOperand sortedOperand : operands)
		{
			mutations.add(sortedOperand.operand());
		}

		mutations.addAll(usings);

		return ReadOnlyList.from(mutations);
	}
}
