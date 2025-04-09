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

	@Override
	public int skipAmount()
	{
		var skipPart = skipToken.source().toLowerCase().replace("x", "");
		return Integer.parseInt(skipPart);
	}

	void setSkipToken(SyntaxToken skipToken)
	{
		this.skipToken = skipToken;
	}
}
