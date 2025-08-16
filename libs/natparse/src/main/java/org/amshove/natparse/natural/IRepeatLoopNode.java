package org.amshove.natparse.natural;

import org.amshove.natparse.natural.conditionals.IConditionNode;

public interface IRepeatLoopNode extends IStatementWithBodyNode, ILabelReferencable
{
	IConditionNode condition();
}
