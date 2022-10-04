package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.ISkipOperandNode;

class SkipOperandNode extends BaseSyntaxNode implements ISkipOperandNode
{
	private SyntaxToken skipToken;

	@Override
	public SyntaxToken skipToken()
	{
		return skipToken;
	}

	void setSkipToken(SyntaxToken skipToken)
	{
		this.skipToken = skipToken;
	}
}
