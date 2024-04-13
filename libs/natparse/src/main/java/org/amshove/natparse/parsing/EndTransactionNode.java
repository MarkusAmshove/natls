package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IEndTransactionNode;
import org.amshove.natparse.natural.IOperandNode;

import java.util.ArrayList;
import java.util.List;

class EndTransactionNode extends StatementNode implements IEndTransactionNode
{
	private final List<IOperandNode> operands = new ArrayList<>();

	@Override
	public ReadOnlyList<IOperandNode> operands()
	{
		return ReadOnlyList.from(operands);
	}

	void addOperand(IOperandNode operand)
	{
		this.operands.add(operand);
	}
}
