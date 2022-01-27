package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxKind;

public interface ISystemVariableNode extends ISyntaxNode
{
	SyntaxKind systemVariable();
}
