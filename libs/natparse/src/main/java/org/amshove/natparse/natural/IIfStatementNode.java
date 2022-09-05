package org.amshove.natparse.natural;

import org.amshove.natparse.natural.conditionals.IConditionNode;

public interface IIfStatementNode extends IStatementWithBodyNode
{
	IConditionNode condition();
}
