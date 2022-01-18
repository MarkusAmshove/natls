package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface IStatementListNode extends ISyntaxNode
{
    ReadOnlyList<IStatementNode> statements();
}
