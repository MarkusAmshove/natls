package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

import javax.annotation.Nullable;

public interface IPerformBreakNode extends IStatementNode
{
	@Nullable
	SyntaxToken statementIdentifier();

	IBreakOfNode breakOf();
}
