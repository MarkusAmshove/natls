package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IIfStatementNode;
import org.amshove.natparse.natural.conditionals.IConditionNode;

class IfStatementNode extends StatementWithBodyNode implements IIfStatementNode
{
	private IConditionNode condition;

	@Override
	public IConditionNode condition()
	{
		return condition;
	}

	void setCondition(ConditionNode condition)
	{
		addNode(condition);
		this.condition = condition;
	}
}
