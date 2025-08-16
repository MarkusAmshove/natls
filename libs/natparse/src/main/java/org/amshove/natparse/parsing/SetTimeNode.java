package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.ISetTimeNode;

class SetTimeNode extends StatementNode implements ISetTimeNode, ILabelIdentifierSettable
{
	private SyntaxToken labelIdentifier;

	@Override
	public SyntaxToken labelIdentifier()
	{
		return labelIdentifier;
	}

	@Override
	public void setLabelIdentifier(SyntaxToken labelIdentifier)
	{
		this.labelIdentifier = labelIdentifier;
	}
}
