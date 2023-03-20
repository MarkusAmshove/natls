package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxKind;

public interface IPostfixUnaryArithmeticExpressionNode extends IOperandNode
{
	SyntaxKind postfixOperator();

	IOperandNode operand();
}
