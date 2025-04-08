package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.ISkipOperandNode;

class SkipOperandNode extends BaseSyntaxNode implements ISkipOperandNode
{
	private SyntaxToken skipToken;
	private int skipAmount = -1;

	@Override
	public SyntaxToken skipToken()
	{
		return skipToken;
	}

	@Override
	public int skipAmount()
	{
		if (skipAmount >= 0)
		{
			return skipAmount;
		}

		var skipPart = skipToken.source().toLowerCase().replace("x", "");
		if (!skipPart.matches("\\d+"))
		{
			return 0;
		}

		skipAmount = Integer.parseInt(skipPart);
		return skipAmount;
	}

	void setSkipToken(SyntaxToken skipToken)
	{
		this.skipToken = skipToken;
	}
}
