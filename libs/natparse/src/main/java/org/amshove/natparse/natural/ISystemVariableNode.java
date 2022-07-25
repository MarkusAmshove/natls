package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxKind;

public interface ISystemVariableNode extends ITokenNode, IOperandNode
{
	SyntaxKind systemVariable();
}
