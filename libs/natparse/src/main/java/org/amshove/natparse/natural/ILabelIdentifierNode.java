package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

public interface ILabelIdentifierNode extends IOperandNode
{
	SyntaxToken label();
}
