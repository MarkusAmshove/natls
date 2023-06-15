package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IAcceptRejectNode;
import org.amshove.natparse.natural.conditionals.IConditionNode;

class AcceptRejectNode extends StatementNode implements IAcceptRejectNode
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
