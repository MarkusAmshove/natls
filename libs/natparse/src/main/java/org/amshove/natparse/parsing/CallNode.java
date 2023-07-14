package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.ICallNode;
import org.amshove.natparse.natural.IMutateVariables;
import org.amshove.natparse.natural.IOperandNode;

import java.util.ArrayList;
import java.util.List;

class CallNode extends StatementNode implements ICallNode, IMutateVariables
{
	private IOperandNode calling;
	private final List<IOperandNode> operands = new ArrayList<>();

	@Override
	public IOperandNode calling()
	{
		return this.calling;
	}

	@Override
	public ReadOnlyList<IOperandNode> operands()
	{
		return ReadOnlyList.from(operands);
	}

	void setCalling(IOperandNode calling)
	{
		this.calling = calling;
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
