package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.ICloseWorkNode;
import org.amshove.natparse.natural.ILiteralNode;

class CloseWorkNode extends StatementNode implements ICloseWorkNode
{
	private ILiteralNode number;

	@Override
	public ILiteralNode number()
	{
		return number;
	}

	void setNumber(ILiteralNode number)
	{
		this.number = number;
	}
}
