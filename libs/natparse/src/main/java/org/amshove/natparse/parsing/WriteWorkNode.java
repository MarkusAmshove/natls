package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.ILiteralNode;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.IWriteWorkNode;

import java.util.ArrayList;
import java.util.List;

class WriteWorkNode extends StatementNode implements IWriteWorkNode
{
	private ILiteralNode number;
	private boolean isVariable;
	private final List<IOperandNode> operands = new ArrayList<>();

	@Override
	public ILiteralNode number()
	{
		return number;
	}

	@Override
	public boolean isVariable()
	{
		return isVariable;
	}

	@Override
	public ReadOnlyList<IOperandNode> operands()
	{
		return ReadOnlyList.from(operands);
	}

	void setNumber(ILiteralNode number)
	{
		this.number = number;
	}

	void setVariable(boolean variable)
	{
		isVariable = variable;
	}

	void addOperand(IOperandNode operand)
	{
		operands.add(operand);
	}
}
