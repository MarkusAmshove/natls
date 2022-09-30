package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.IRangedArrayAccessNode;

class RangedArrayAccessNode extends BaseSyntaxNode implements IRangedArrayAccessNode
{
	private IOperandNode lowerBound;
	private IOperandNode upperBound;


	@Override
	public IOperandNode lowerBound()
	{
		return lowerBound;
	}

	@Override
	public IOperandNode upperBound()
	{
		return upperBound;
	}

	void setLowerBound(IOperandNode lowerBound)
	{
		this.lowerBound = lowerBound;
	}

	void setUpperBound(IOperandNode upperBound)
	{
		this.upperBound = upperBound;
	}
}
