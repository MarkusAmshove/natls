package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IReadNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class ReadNode extends StatementWithBodyNode implements IReadNode
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
