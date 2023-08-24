package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IStatementNode;
import org.amshove.natparse.natural.IStatementVisitor;

class StatementNode extends BaseSyntaxNode implements IStatementNode
{
	@Override
	public void acceptStatementVisitor(IStatementVisitor visitor)
	{
		visitor.visit(this);
	}
}
