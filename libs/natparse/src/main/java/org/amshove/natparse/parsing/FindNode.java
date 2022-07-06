package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IFindNode;
import org.amshove.natparse.natural.ISymbolReferenceNode;

class FindNode extends StatementWithBodyNode implements IFindNode
{
	private SymbolReferenceNode view;

	void setView(SymbolReferenceNode view)
	{
		addNode(view);
		this.view = view;
	}

	@Override
	public ISymbolReferenceNode viewReference()
	{
		return view;
	}
}
