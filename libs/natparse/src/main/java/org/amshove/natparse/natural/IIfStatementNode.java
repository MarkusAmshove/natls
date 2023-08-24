package org.amshove.natparse.natural;

import org.amshove.natparse.natural.conditionals.IConditionNode;

import javax.annotation.Nullable;

public interface IIfStatementNode extends IStatementWithBodyNode
{
	IConditionNode condition();

	@Nullable
	IStatementListNode elseBranch();
}
