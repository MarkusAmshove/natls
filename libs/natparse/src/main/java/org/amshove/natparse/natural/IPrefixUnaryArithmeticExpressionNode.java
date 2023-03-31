package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxKind;

public interface IPrefixUnaryArithmeticExpressionNode extends IOperandNode
{
	SyntaxKind postfixOperator();

	IOperandNode operand();
}
