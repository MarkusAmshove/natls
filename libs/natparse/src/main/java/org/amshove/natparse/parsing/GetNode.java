package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IGetNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class GetNode extends StatementNode implements IGetNode
{
	private IVariableReferenceNode view;

	void setView(IVariableReferenceNode view)
	{
		this.view = view;
	}

	@Override
	public IVariableReferenceNode viewReference()
	{
		return view;
	}
}
