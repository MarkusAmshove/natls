package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.IPrefixUnaryArithmeticExpressionNode;

class PrefixUnaryArithmeticExpressionNode extends BaseSyntaxNode implements IPrefixUnaryArithmeticExpressionNode
{
	private SyntaxKind postfixOperator;
	private IOperandNode operand;

	@Override
	public SyntaxKind postfixOperator()
	{
		return postfixOperator;
	}

	@Override
	public IOperandNode operand()
	{
		return operand;
	}

	void setOperand(IOperandNode operand)
	{
		this.operand = operand;
	}

	void setPostfixOperator(SyntaxKind postfixOperator)
	{
		this.postfixOperator = postfixOperator;
	}
}
