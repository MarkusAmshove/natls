package org.amshove.natparse.natural;

import org.amshove.natparse.natural.conditionals.IConditionNode;

public interface IIfSelectionNode extends IStatementWithBodyNode
{
	IConditionNode condition();
}
