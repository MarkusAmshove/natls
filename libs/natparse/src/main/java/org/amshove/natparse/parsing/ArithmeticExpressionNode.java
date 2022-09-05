package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.IArithmeticExpressionNode;
import org.amshove.natparse.natural.IOperandNode;

class ArithmeticExpressionNode extends BaseSyntaxNode implements IArithmeticExpressionNode
{
	private IOperandNode left;
	private SyntaxKind operator;
	private IOperandNode right;

	@Override
	public IOperandNode left()
	{
		return left;
	}

	@Override
	public SyntaxKind operator()
	{
		return operator;
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

	void setOperator(SyntaxKind operator)
	{
		this.operator = operator;
	}

	void setRight(IOperandNode right)
	{
		this.right = right;
	}
}
