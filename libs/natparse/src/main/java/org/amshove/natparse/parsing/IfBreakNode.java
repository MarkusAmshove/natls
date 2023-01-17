package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IIfBreakNode;
import org.amshove.natparse.natural.conditionals.IConditionNode;

class IfBreakNode extends StatementWithBodyNode implements IIfBreakNode
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
