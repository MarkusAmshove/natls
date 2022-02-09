package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IIncludeNode;
import org.amshove.natparse.natural.IStatementListNode;

class IncludeNode extends ModuleReferencingNode implements IIncludeNode
{
	private IStatementListNode body;

	@Override
	public IStatementListNode body()
	{
		return body;
	}

	void setBody(IStatementListNode body)
	{
		addNode((StatementListNode) body);
		this.body = body;
	}
}
