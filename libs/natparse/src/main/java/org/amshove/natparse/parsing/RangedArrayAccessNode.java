package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
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

	@Override
	public boolean isAnyUnbound()
	{
		return isUnbound(lowerBound) || isUnbound(upperBound);
	}

	private boolean isUnbound(IOperandNode operand)
	{
		return operand instanceof LiteralNode literal && literal.token().kind() == SyntaxKind.ASTERISK;
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
