package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.ILiteralNode;
import org.amshove.natparse.natural.IReadWorkNode;

class ReadWorkNode extends StatementWithBodyNode implements IReadWorkNode
{
	private ILiteralNode workFileNumber;

	@Override
	public ILiteralNode workFileNumber()
	{
		return workFileNumber;
	}

	void setWorkFileNumber(ILiteralNode workFileNumber)
	{
		this.workFileNumber = workFileNumber;
	}
}
