package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IRepeatLoopNode;
import org.amshove.natparse.natural.conditionals.IConditionNode;
import org.jspecify.annotations.Nullable;

class RepeatLoopNode extends StatementWithBodyNode implements IRepeatLoopNode, ILabelIdentifierSettable
{
	private IConditionNode condition;
	private SyntaxToken labelIdentifier;

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

	@Override
	public @Nullable SyntaxToken labelIdentifier()
	{
		return labelIdentifier;
	}

	@Override
	public void setLabelIdentifier(SyntaxToken labelIdentifier)
	{
		this.labelIdentifier = labelIdentifier;
	}
}
