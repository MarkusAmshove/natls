package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IInternalPerformNode;
import org.amshove.natparse.natural.IReferencableNode;

final class InternalPerformNode extends PerformNode implements IInternalPerformNode
{
	private SyntaxToken callToken;
	private IReferencableNode reference;

	@Override
	public IReferencableNode reference()
	{
		return reference;
	}

	@Override
	public SyntaxToken token()
	{
		return callToken;
	}

	void setCallToken(SyntaxToken symbol)
	{
		callToken = symbol;
	}

	void setReference(IReferencableNode referencableNode)
	{
		reference = referencableNode;
	}
}
