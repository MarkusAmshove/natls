package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.ITokenNode;

// TODO: Only exists until all statements are parseable
class SyntheticTokenStatementNode extends StatementNode implements ITokenNode
{
	@Override
	public SyntaxToken token()
	{
		return ((TokenNode) descendants().first()).token();
	}
}
