package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IFindNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class FindNode extends StatementWithBodyNode implements IFindNode, ILabelIdentifierSettable
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
	public SyntaxToken labelIdentifier()
	{
		return labelIdentifier;
	}

	@Override
	public void setLabelIdentifier(SyntaxToken labelIdentifier)
	{
		this.labelIdentifier = labelIdentifier;
	}
}
