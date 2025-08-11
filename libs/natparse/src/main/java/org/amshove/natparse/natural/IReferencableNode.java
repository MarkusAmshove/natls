package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;
import org.jspecify.annotations.NonNull;

public interface IReferencableNode extends ISymbolNode
{
	@NonNull
	ReadOnlyList<ISymbolReferenceNode> references();

	void removeReference(ISymbolReferenceNode node);

	void addReference(ISymbolReferenceNode node);
}
