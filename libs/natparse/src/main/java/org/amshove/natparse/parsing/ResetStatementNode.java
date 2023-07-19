package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.IResetStatementNode;

import java.util.ArrayList;
import java.util.List;

class ResetStatementNode extends StatementNode implements IResetStatementNode
{
	private final List<IOperandNode> operands = new ArrayList<>();

	@Override
	public ReadOnlyList<IOperandNode> operands()
	{
		return ReadOnlyList.from(operands);
	}

	void addOperand(IOperandNode operandNode)
	{
		operands.add(operandNode);
	}

	@Override
	public ReadOnlyList<IOperandNode> mutations()
	{
		return ReadOnlyList.from(operands);
	}
}
