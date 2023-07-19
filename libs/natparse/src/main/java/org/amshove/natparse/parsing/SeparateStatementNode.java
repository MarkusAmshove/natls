package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.ISeparateStatementNode;
import org.amshove.natparse.natural.IMutateVariables;
import org.amshove.natparse.ReadOnlyList;

import java.util.ArrayList;
import java.util.List;

class SeparateStatementNode extends StatementNode implements ISeparateStatementNode, IMutateVariables
{
	private final List<IOperandNode> targets = new ArrayList<>();

	private IOperandNode separated;

	@Override
	public IOperandNode separated()
	{
		return separated;
	}

	void setSeparated(IOperandNode separated)
	{
		this.separated = separated;
	}

	@Override
	public ReadOnlyList<IOperandNode> targets()
	{
		return ReadOnlyList.from(targets);
	}

	void addTarget(IOperandNode operand)
	{
		targets.add(operand);
	}

	@Override
	public ReadOnlyList<IOperandNode> mutations()
	{
		return ReadOnlyList.from(targets);
	}
}
