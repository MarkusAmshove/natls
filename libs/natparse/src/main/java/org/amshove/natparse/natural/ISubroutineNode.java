package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

public interface ISubroutineNode extends IStatementNode, IReferencableNode
{
	SyntaxToken name();
	IStatementListNode body();
}
