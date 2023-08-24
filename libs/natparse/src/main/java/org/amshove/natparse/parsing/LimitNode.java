package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.ILimitNode;
import org.amshove.natparse.natural.ILiteralNode;

class LimitNode extends StatementNode implements ILimitNode
{
	private ILiteralNode limit;

	@Override
	public ILiteralNode limit()
	{
		return limit;
	}

	void setLimit(ILiteralNode limit)
	{
		this.limit = limit;
	}
}
