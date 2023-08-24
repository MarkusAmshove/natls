package org.amshove.natparse.parsing;

import java.util.Optional;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IGetSameNode;

class GetSameNode extends StatementNode implements IGetSameNode
{
	private SyntaxToken label;

	@Override
	public Optional<SyntaxToken> label()
	{
		return Optional.ofNullable(label);
	}

	void setLabel(SyntaxToken label)
	{
		this.label = label;
	}
}
