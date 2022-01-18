package org.amshove.natparse.parsing;

import java.util.ArrayList;
import java.util.List;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IStatementListNode;
import org.amshove.natparse.natural.IStatementNode;

class StatementListNode extends BaseSyntaxNode implements IStatementListNode
{
    private final List<IStatementNode> statements = new ArrayList<>();

	@Override
	public ReadOnlyList<IStatementNode> statements()
    {
		return ReadOnlyList.from(statements);
	}

    void addStatement(StatementNode statement)
    {
        statements.add(statement);
        statement.setParent(this);
    }
}
