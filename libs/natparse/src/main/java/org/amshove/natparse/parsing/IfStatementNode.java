package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IIfStatementNode;
import org.amshove.natparse.natural.IStatementListNode;

class IfStatementNode extends StatementListNode implements IIfStatementNode
{
	private StatementListNode body;

	public IStatementListNode body()
	{
		return body;
	}

	void setBody(StatementListNode body)
	{
		for (var statement : body.statements())
		{
			addStatement((StatementNode) statement);
		}
		this.body = body;
	}
}
