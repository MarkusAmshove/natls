package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IStatementListNode;

class StatementWithBodyNode extends StatementListNode
{
	public IStatementListNode body()
	{
		return this;
	}

	void setBody(StatementListNode body)
	{
		for (var statement : body.statements())
		{
			addStatement((StatementNode) statement);
		}
	}
}
