package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxKind;

public interface ISystemVariableNode extends ISymbolNode
{
	SyntaxKind systemVariable();
}
