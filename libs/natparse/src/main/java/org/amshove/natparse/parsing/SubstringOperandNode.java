package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.ISubstringOperandNode;

import java.util.Optional;

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
	public Optional<IOperandNode> startPosition()
	{
		return Optional.ofNullable(startingPosition);
	}

	@Override
	public Optional<IOperandNode> length()
	{
		return Optional.ofNullable(length);
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
