package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IHistogramNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class HistogramNode extends StatementWithBodyNode implements IHistogramNode
{
	private IVariableReferenceNode view;
	private SyntaxToken descriptor;

	@Override
	public IVariableReferenceNode view()
	{
		return view;
	}

	@Override
	public SyntaxToken descriptor()
	{
		return descriptor;
	}

	void setView(IVariableReferenceNode view)
	{
		this.view = view;
	}

	void setDescriptor(SyntaxToken descriptor)
	{
		this.descriptor = descriptor;
	}
}
