package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IArrayDimension;

class ArrayDimension extends BaseSyntaxNode implements IArrayDimension
{
	private int lowerBound;
	private int upperBound;
	private boolean upperVariable;

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

	@Override
	public boolean isUpperVariable()
	{
		return upperVariable || upperBound == VARIABLE_BOUND;
	}

	void setLowerBound(int bound)
	{
		this.lowerBound = bound;
	}

	void setUpperBound(int bound)
	{
		this.upperBound = bound;
	}

	void setUpperVariable()
	{
		this.upperVariable = true;
	}
}
