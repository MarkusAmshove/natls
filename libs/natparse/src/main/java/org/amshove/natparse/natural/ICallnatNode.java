package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

public interface ICallnatNode extends IStatementNode
{
	SyntaxToken calledModule();
}
