package org.amshove.natparse.natural;

import org.amshove.natparse.natural.conditionals.ILogicalConditionCriteriaNode;

public interface IDecideForConditionBranchNode extends IStatementWithBodyNode
{
	ILogicalConditionCriteriaNode criteria();
}
