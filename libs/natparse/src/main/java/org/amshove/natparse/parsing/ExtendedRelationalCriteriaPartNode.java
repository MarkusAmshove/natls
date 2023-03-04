package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.conditionals.IExtendedRelationalCriteriaPartNode;

class ExtendedRelationalCriteriaPartNode extends BaseSyntaxNode implements IExtendedRelationalCriteriaPartNode
{
	private IOperandNode rhs;
	private SyntaxToken comparisonToken;

	@Override
	public IOperandNode rhs()
	{
		return rhs;
	}

	@Override
	public SyntaxToken comparisonToken()
	{
		return comparisonToken;
	}

	void setRhs(IOperandNode operand)
	{
		rhs = operand;
	}

	void setComparisonToken(SyntaxToken comparisonToken)
	{
		this.comparisonToken = comparisonToken;
	}
}
