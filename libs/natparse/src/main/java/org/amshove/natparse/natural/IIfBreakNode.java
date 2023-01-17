package org.amshove.natparse.natural;

import org.amshove.natparse.natural.conditionals.IConditionNode;

public interface IIfBreakNode extends IStatementWithBodyNode
{
	IConditionNode condition();
}
