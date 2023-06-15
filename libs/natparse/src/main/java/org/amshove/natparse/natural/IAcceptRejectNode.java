package org.amshove.natparse.natural;

import org.amshove.natparse.natural.conditionals.IConditionNode;

public interface IAcceptRejectNode extends IStatementNode
{
	IConditionNode condition();
}
