package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IReferencableNode;
import org.amshove.natparse.natural.ISymbolReferenceNode;

class SymbolReferenceNode extends TokenNode implements ISymbolReferenceNode
{
	private IReferencableNode reference;

	public SymbolReferenceNode(SyntaxToken token)
	{
		super(token);
	}

	@Override
	public IReferencableNode reference()
	{
		return reference;
	}

	@Override
	public SyntaxToken referencingToken()
	{
		return super.token();
	}

	void setReference(IReferencableNode symbolNode)
	{
		reference = symbolNode;
	}

	@Override
	public void destroy()
	{
		if (reference == null)
		{
			return;
		}

		reference.removeReference(this);
		reference = null;
		super.destroy();
	}
}
