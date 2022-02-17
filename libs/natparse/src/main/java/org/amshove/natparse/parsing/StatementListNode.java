package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IStatementListNode;
import org.amshove.natparse.natural.IStatementNode;

import java.util.ArrayList;
import java.util.List;

class StatementListNode extends StatementNode implements IStatementListNode
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
		addNode(statement);
		statement.setParent(this);
	}

	@Override
	protected void replaceChild(BaseSyntaxNode oldChild, BaseSyntaxNode newChild)
	{
		super.replaceChild(oldChild, newChild);
		if(oldChild instanceof StatementNode oldStatement && newChild instanceof StatementNode newStatement)
		{
			var oldIndex = statements.indexOf(oldStatement);
			if(oldIndex < 0)
			{
				return;
			}
			statements.set(oldIndex, newStatement);
		}
	}
}
