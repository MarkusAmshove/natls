package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IEscapeNode;

import java.util.Optional;

class EscapeNode extends StatementNode implements IEscapeNode
{
	private SyntaxKind escapeDirection;
	private boolean isReposition;
	private boolean isImmediate;
	private SyntaxToken label;

	@Override
	public SyntaxKind escapeDirection()
	{
		return escapeDirection;
	}

	@Override
	public boolean isReposition()
	{
		return isReposition;
	}

	@Override
	public boolean isImmediate()
	{
		return isImmediate;
	}

	@Override
	public Optional<SyntaxToken> label()
	{
		return Optional.ofNullable(label);
	}

	void setLabel(SyntaxToken label)
	{
		this.label = label;
	}

	void setImmediate()
	{
		isImmediate = true;
	}

	void setReposition()
	{
		isReposition = true;
	}

	void setDirection(SyntaxKind kind)
	{
		escapeDirection = kind;
	}
}
