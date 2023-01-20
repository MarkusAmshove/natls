package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IIfSelectionNode;
import org.amshove.natparse.natural.conditionals.IConditionNode;

class IfSelectionNode extends StatementWithBodyNode implements IIfSelectionNode
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
