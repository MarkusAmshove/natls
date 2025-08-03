package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface IOpenConversationNode extends IStatementNode
{
	ReadOnlyList<IOperandNode> subprograms();
}
