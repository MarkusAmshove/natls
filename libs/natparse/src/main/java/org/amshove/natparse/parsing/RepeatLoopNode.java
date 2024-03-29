package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IRepeatLoopNode;
import org.amshove.natparse.natural.conditionals.IConditionNode;

class RepeatLoopNode extends StatementWithBodyNode implements IRepeatLoopNode
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
