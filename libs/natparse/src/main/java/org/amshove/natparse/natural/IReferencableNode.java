package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface IReferencableNode extends ISyntaxNode
{
	ReadOnlyList<ISymbolReferenceNode> references();
}
