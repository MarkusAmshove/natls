package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

import javax.annotation.Nonnull;

public interface IReferencableNode extends ISyntaxNode, ISymbolNode
{
	@Nonnull
	ReadOnlyList<ISymbolReferenceNode> references();

	void removeReference(ISymbolReferenceNode node);

	void addReference(ISymbolReferenceNode node);
}
