package org.amshove.natparse.natural;

import java.util.Optional;

import org.amshove.natparse.ReadOnlyList;

public interface IDecideForConditionNode extends IStatementNode
{
	ReadOnlyList<IDecideForConditionBranchNode> branches();
	Optional<IStatementListNode> whenAny();
	Optional<IStatementListNode> whenAll();
	IStatementListNode whenNone();
}
