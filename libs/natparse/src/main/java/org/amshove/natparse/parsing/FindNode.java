package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IFindNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class FindNode extends StatementWithBodyNode implements IFindNode
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
