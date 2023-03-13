package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IComputeStatementNode;
import org.amshove.natparse.natural.IOperandNode;

class ComputeStatementNode extends StatementNode implements IComputeStatementNode
{
	private IOperandNode target;
	private IOperandNode operand;
	private boolean isRounded;

	@Override
	public IOperandNode target()
	{
		return target;
	}

	@Override
	public IOperandNode operand()
	{
		return operand;
	}

	@Override
	public boolean isRounded()
	{
		return isRounded;
	}

	void setTarget(IOperandNode target)
	{
		this.target = target;
	}

	void setOperand(IOperandNode operand)
	{
		this.operand = operand;
	}

	void setRounded(boolean rounded)
	{
		isRounded = rounded;
	}
}
