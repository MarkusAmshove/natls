package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.ITerminateNode;

import java.util.ArrayList;
import java.util.List;

class TerminateNode extends StatementNode implements ITerminateNode
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
}
