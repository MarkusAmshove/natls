package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.conditionals.IRangedExtendedRelationalCriteriaNode;

import java.util.Optional;

class RangedExtendedRelationalCriteriaNode extends BaseSyntaxNode implements IRangedExtendedRelationalCriteriaNode
{
	private IOperandNode left;
	private IOperandNode lowerBound;
	private IOperandNode upperBound;
	private IOperandNode excludedLowerBound;
	private IOperandNode excludedUpperBound;

	public RangedExtendedRelationalCriteriaNode(RelationalCriteriaNode expression)
	{
		copyFrom(expression);
		left = expression.left();
		lowerBound = expression.right();
	}

	@Override
	public IOperandNode left()
	{
		return left;
	}

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

	@Override
	public Optional<IOperandNode> excludedLowerBound()
	{
		return Optional.ofNullable(excludedLowerBound);
	}

	@Override
	public Optional<IOperandNode> excludedUpperBound()
	{
		return Optional.ofNullable(excludedUpperBound);
	}

	void setLeft(IOperandNode node)
	{
		left = node;
	}

	void setLowerBound(IOperandNode node)
	{
		lowerBound = node;
	}

	void setUpperBound(IOperandNode node)
	{
		upperBound = node;
	}

	void setExcludedLowerBound(IOperandNode node)
	{
		excludedLowerBound = node;
	}

	void setExcludedUpperBound(IOperandNode node)
	{
		excludedUpperBound = node;
	}
}
