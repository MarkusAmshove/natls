package org.amshove.natparse.natural.conditionals;

import org.amshove.natparse.natural.ISyntaxNode;

public interface IConditionNode extends ISyntaxNode
{
	ILogicalConditionCriteriaNode criteria();
}
