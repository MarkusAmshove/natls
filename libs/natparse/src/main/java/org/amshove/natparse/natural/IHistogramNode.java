package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.conditionals.IConditionNode;

public interface IHistogramNode extends IStatementWithBodyNode, IAdabasAccessStatementNode
{
	SyntaxToken descriptor();

	IConditionNode condition();
}
