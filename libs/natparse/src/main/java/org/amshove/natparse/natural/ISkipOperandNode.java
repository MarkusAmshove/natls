package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

public interface ISkipOperandNode extends IOperandNode
{
	SyntaxToken skipToken();

	int skipAmount();
}
