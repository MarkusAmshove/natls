package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.ISeparateStatementNode;
import org.amshove.natparse.ReadOnlyList;

import java.util.ArrayList;
import java.util.List;

class SeparateStatementNode extends StatementNode implements ISeparateStatementNode
{
	private final List<IOperandNode> intoList = new ArrayList<>();

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
	public ReadOnlyList<IOperandNode> intoList()
	{
		return ReadOnlyList.from(intoList);
	}

	void addOperand(IOperandNode operand)
	{
		intoList.add(operand);
	}

}
