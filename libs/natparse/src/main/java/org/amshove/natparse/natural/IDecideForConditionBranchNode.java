package org.amshove.natparse.natural;

import org.amshove.natparse.natural.conditionals.IConditionNode;

public interface IDecideForConditionBranchNode extends IStatementWithBodyNode
{
	IConditionNode criteria();
}
