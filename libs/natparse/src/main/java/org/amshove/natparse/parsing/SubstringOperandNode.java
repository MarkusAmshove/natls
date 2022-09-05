package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.ISubstringOperandNode;

class SubstringOperandNode extends BaseSyntaxNode implements ISubstringOperandNode
{
	private IOperandNode operand;
	private IOperandNode startingPosition;
	private IOperandNode length;

	@Override
	public IOperandNode operand()
	{
		return operand;
	}

	@Override
	public IOperandNode startPosition()
	{
		return startingPosition;
	}

	@Override
	public IOperandNode length()
	{
		return length;
	}

	void setOperand(IOperandNode operand)
	{
		this.operand = operand;
	}

	void setStartingPosition(IOperandNode startingPosition)
	{
		this.startingPosition = startingPosition;
	}

	void setLength(IOperandNode length)
	{
		this.length = length;
	}
}
