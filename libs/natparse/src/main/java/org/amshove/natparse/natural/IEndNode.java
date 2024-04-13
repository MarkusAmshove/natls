package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

public interface IEndNode extends IStatementNode
{
	SyntaxToken token();
}
