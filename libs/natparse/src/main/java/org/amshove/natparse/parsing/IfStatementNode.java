package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IIfStatementNode;
import org.amshove.natparse.natural.IStatementListNode;
import org.amshove.natparse.natural.conditionals.IConditionNode;

import javax.annotation.Nullable;

class IfStatementNode extends StatementWithBodyNode implements IIfStatementNode
{
	private IConditionNode condition;
	private StatementListNode elseBranch;

	@Override
	public IConditionNode condition()
	{
		return condition;
	}

	@Nullable
	@Override
	public IStatementListNode elseBranch()
	{
		return elseBranch;
	}

	void setCondition(ConditionNode condition)
	{
		addNode(condition);
		this.condition = condition;
	}

	void setElseBranch(StatementListNode elseBranch)
	{
		this.elseBranch = elseBranch;
		addNode(elseBranch);
	}
}
