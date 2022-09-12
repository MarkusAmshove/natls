package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxKind;

public interface IArithmeticExpressionNode extends IOperandNode
{
	IOperandNode left();
	SyntaxKind operator();
	IOperandNode right();
}
