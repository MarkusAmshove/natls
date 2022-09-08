package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface IReferencableNode extends ISyntaxNode, ISymbolNode
{
	ReadOnlyList<ISymbolReferenceNode> references();

	void removeReference(ISymbolReferenceNode node);
	void addReference(ISymbolReferenceNode node);
}
