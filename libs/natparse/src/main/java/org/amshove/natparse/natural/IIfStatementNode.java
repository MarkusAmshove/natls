package org.amshove.natparse.natural;

import org.amshove.natparse.natural.conditionals.IConditionNode;
import org.jspecify.annotations.Nullable;

public interface IIfStatementNode extends IStatementWithBodyNode
{
	IConditionNode condition();

	@Nullable
	IStatementListNode elseBranch();
}
