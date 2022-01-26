package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IReferencableNode;
import org.amshove.natparse.natural.ISymbolNode;
import org.amshove.natparse.natural.ISymbolReferenceNode;

class SymbolReferenceNode extends TokenNode implements ISymbolReferenceNode
{
	private ISymbolNode reference;

	public SymbolReferenceNode(SyntaxToken token)
	{
		super(token);
	}

	@Override
	public IReferencableNode reference()
	{
		return reference;
	}

	void setReference(ISymbolNode symbolNode)
	{
		reference = symbolNode;
	}

	@Override
	public void destroy()
	{
		if(reference == null)
		{
			return;
		}

		reference.removeReference(this);
		reference = null;
		super.destroy();
	}
}
