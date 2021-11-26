package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IArrayDimension;

class ArrayDimension extends BaseSyntaxNode implements IArrayDimension
{
	private int lowerBound;
	private int upperBound;

	@Override
	public int lowerBound()
	{
		return lowerBound;
	}

	@Override
	public int upperBound()
	{
		return upperBound;
	}

	void setLowerBound(int bound)
	{
		this.lowerBound = bound;
	}

	void setUpperBound(int bound)
	{
		this.upperBound = bound;
	}
}
