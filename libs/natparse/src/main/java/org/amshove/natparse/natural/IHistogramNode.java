package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

public interface IHistogramNode extends IStatementWithBodyNode
{
	IVariableReferenceNode view();

	SyntaxToken descriptor();
}
