package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IGetNode;
import org.amshove.natparse.natural.IVariableReferenceNode;
import org.jspecify.annotations.Nullable;

class GetNode extends StatementNode implements IGetNode, ILabelIdentifierSettable
{
	private IVariableReferenceNode view;
	private SyntaxToken labelIdentifier;

	void setView(IVariableReferenceNode view)
	{
		this.view = view;
	}

	@Override
	public IVariableReferenceNode view()
	{
		return view;
	}

	@Override
	public @Nullable SyntaxToken labelIdentifier()
	{
		return labelIdentifier;
	}

	@Override
	public void setLabelIdentifier(SyntaxToken labelIdentifier)
	{
		this.labelIdentifier = labelIdentifier;
	}
}
