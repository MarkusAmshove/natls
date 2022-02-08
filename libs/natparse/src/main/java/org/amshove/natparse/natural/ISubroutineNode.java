package org.amshove.natparse.natural;

public interface ISubroutineNode extends IStatementNode, IReferencableNode, ISymbolNode
{
	IStatementListNode body();
}
