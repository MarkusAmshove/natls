package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IBreakOfNode;
import org.amshove.natparse.natural.IPerformBreakNode;

import javax.annotation.Nullable;

class PerformBreakNode extends StatementNode implements IPerformBreakNode
{
	private SyntaxToken identifier;
	private IBreakOfNode breakOf;

	@Override
	@Nullable
	public SyntaxToken statementIdentifier()
	{
		return identifier;
	}

	@Override
	public IBreakOfNode breakOf()
	{
		return breakOf;
	}

	void setStatementIdentifier(SyntaxToken token)
	{
		identifier = token;
	}

	void setBreakOf(BreakOfNode breakOf)
	{
		this.breakOf = breakOf;
		breakOf.setParent(this);
	}
}
