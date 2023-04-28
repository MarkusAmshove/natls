package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IClosePcNode;
import org.amshove.natparse.natural.ILiteralNode;

class ClosePcNode extends StatementNode implements IClosePcNode
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
