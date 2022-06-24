package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IInternalPerformNode;
import org.amshove.natparse.natural.IReferencableNode;
import org.amshove.natparse.natural.ITokenNode;

final class InternalPerformNode extends PerformNode implements IInternalPerformNode
{
	private IReferencableNode reference;
	private SymbolReferenceNode referenceNode;

	@Override
	public IReferencableNode reference()
	{
		return reference;
	}

	@Override
	public SyntaxToken referencingToken()
	{
		return token();
	}

	@Override
	public SyntaxToken token()
	{
		return referenceNode.token();
	}

	void setReference(IReferencableNode referencableNode)
	{
		reference = referencableNode;
		referenceNode.setReference(referencableNode);
	}

	ITokenNode tokenNode()
	{
		return findDescendantToken(SyntaxKind.IDENTIFIER);
	}

	void setReferenceNode(SymbolReferenceNode referenceNode)
	{
		this.referenceNode = referenceNode;
	}
}
