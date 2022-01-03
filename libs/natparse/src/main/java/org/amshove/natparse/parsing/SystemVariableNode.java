package org.amshove.natparse.parsing;

import org.amshove.natparse.IPosition;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.ISymbolReferenceNode;
import org.amshove.natparse.natural.ISystemVariableNode;

class SystemVariableNode extends BaseSyntaxNode implements ISystemVariableNode
{
	private final SyntaxToken declaration;

	SystemVariableNode(SyntaxToken declaration)
	{
		this.declaration = declaration;
		addNode(new TokenNode(declaration));
	}

	@Override
	@SuppressWarnings("unchecked")
	public ReadOnlyList<ISymbolReferenceNode> references()
	{
		return ReadOnlyList.EMPTY;
	}

	@Override
	public SyntaxToken declaration()
	{
		return declaration;
	}

	@Override
	public IPosition position()
	{
		return declaration;
	}

	@Override
	public SyntaxKind systemVariable()
	{
		return declaration.kind();
	}
}
