package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IReadNode;
import org.amshove.natparse.natural.IVariableReferenceNode;
import org.amshove.natparse.natural.ReadSequence;

class ReadNode extends StatementWithBodyNode implements IReadNode
{
	private IVariableReferenceNode view;
	private ReadSequence readSequence;

	@Override
	public IVariableReferenceNode view()
	{
		return view;
	}

	@Override
	public ReadSequence readSequence()
	{
		return readSequence;
	}

	void setView(IVariableReferenceNode view)
	{
		this.view = view;
	}

	void setReadSequence(ReadSequence readSequence)
	{
		this.readSequence = readSequence;
	}
}
