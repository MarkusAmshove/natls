package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IReadNode;
import org.amshove.natparse.natural.IVariableReferenceNode;
import org.amshove.natparse.natural.ReadSequence;
import org.jspecify.annotations.Nullable;

class ReadNode extends StatementWithBodyNode implements IReadNode, ILabelIdentifierSettable
{
	private IVariableReferenceNode view;
	private ReadSequence readSequence;
	private SyntaxToken labelIdentifier;

	@Override
	public IVariableReferenceNode view()
	{
		return view;
	}

	@Override
	public ReadSequence readSequence()
	{
		return readSequence;
	}

	void setView(IVariableReferenceNode view)
	{
		this.view = view;
	}

	void setReadSequence(ReadSequence readSequence)
	{
		this.readSequence = readSequence;
	}

	@Override
	public @Nullable SyntaxToken labelIdentifier()
	{
		return labelIdentifier;
	}

	@Override
	public void setLabelIdentifier(SyntaxToken labelIdentifier)
	{
		this.labelIdentifier = labelIdentifier;
	}
}
