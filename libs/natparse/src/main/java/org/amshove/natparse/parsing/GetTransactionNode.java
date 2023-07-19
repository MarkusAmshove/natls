package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.IGetTransactionNode;
import org.amshove.natparse.ReadOnlyList;

import java.util.ArrayList;
import java.util.List;

class GetTransactionNode extends StatementNode implements IGetTransactionNode
{
	private final List<IOperandNode> operands = new ArrayList<>();

	@Override
	public ReadOnlyList<IOperandNode> operands()
	{
		return ReadOnlyList.from(operands);
	}

	void addOperand(IOperandNode operand)
	{
		operands.add(operand);
	}

	@Override
	public ReadOnlyList<IOperandNode> mutations()
	{
		return ReadOnlyList.from(operands);
	}
}
