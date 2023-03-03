package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.conditionals.ComparisonOperator;
import org.amshove.natparse.natural.conditionals.IRelationalCriteriaNode;

class RelationalCriteriaNode extends BaseSyntaxNode implements IRelationalCriteriaNode
{
	private IOperandNode left;
	private ComparisonOperator operator;
	private IOperandNode right;
	private SyntaxToken comparisonToken;

	@Override
	public IOperandNode left()
	{
		return left;
	}

	@Override
	public ComparisonOperator operator()
	{
		return operator;
	}

	@Override
	public SyntaxToken comparisonToken()
	{
		return comparisonToken;
	}

	@Override
	public IOperandNode right()
	{
		return right;
	}

	void setLeft(IOperandNode left)
	{
		this.left = left;
	}

	void setOperator(ComparisonOperator operator)
	{
		this.operator = operator;
	}

	void setRight(IOperandNode right)
	{
		this.right = right;
	}

	void setComparisonToken(SyntaxToken token)
	{
		this.comparisonToken = token;
	}
}
