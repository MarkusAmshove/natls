package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

import javax.annotation.Nonnull;

public interface IReferencableNode extends ISyntaxNode
{
	@Nonnull
	ReadOnlyList<ISymbolReferenceNode> references();

	void removeReference(ISymbolReferenceNode node);
}
