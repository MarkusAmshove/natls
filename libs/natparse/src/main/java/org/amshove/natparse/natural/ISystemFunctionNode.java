package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxKind;

public interface ISystemFunctionNode extends IOperandNode
{
	SyntaxKind systemFunction();
	IOperandNode parameter();
}
