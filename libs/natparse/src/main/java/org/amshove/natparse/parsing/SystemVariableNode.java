package org.amshove.natparse.parsing;

import org.amshove.natparse.IPosition;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
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
