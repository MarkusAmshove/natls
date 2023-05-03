package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.ILiteralNode;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.IWritePcNode;

class WritePcNode extends StatementNode implements IWritePcNode
{
	private IOperandNode operand;
	private ILiteralNode number;
	private boolean isVariable;

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
	public IOperandNode operand()
	{
		return operand;
	}

	void setNumber(ILiteralNode literalNode)
	{
		number = literalNode;
	}

	void setOperand(IOperandNode operand)
	{
		this.operand = operand;
	}

	void setVariable(boolean isVariable)
	{
		this.isVariable = isVariable;
	}
}
