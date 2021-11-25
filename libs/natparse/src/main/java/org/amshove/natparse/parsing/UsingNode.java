package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IUsingNode;

class UsingNode extends BaseSyntaxNode implements IUsingNode
{
	private boolean isLocal = false;
	private boolean isParameter = false;
	private boolean isGlobal = false;
	private SyntaxToken using;

	@Override
	public SyntaxToken target()
	{
		return using;
	}

	@Override
	public boolean isLocalUsing()
	{
		return isLocal;
	}

	@Override
	public boolean isGlobalUsing()
	{
		return isGlobal;
	}

	@Override
	public boolean isParameterUsing()
	{
		return isParameter;
	}

	void setUsingTarget(SyntaxToken using)
	{
		this.using = using;
	}

	private void setLocal()
	{
		isLocal = true;
		isParameter = false;
		isGlobal = false;
	}

	private void setParameter()
	{
		isLocal = false;
		isParameter = true;
		isGlobal = false;
	}

	private void setGlobal()
	{
		isLocal = false;
		isParameter = false;
		isGlobal = true;
	}

	void setScope(SyntaxKind scopeKind)
	{
		switch (scopeKind)
		{
			case LOCAL:
				setLocal();
				break;
			case PARAMETER:
				setParameter();
				break;
			case GLOBAL:
				setGlobal();
		}
	}

	@Override public String toString()
	{
		return "UsingNode{isLocal=%s, isParameter=%s, isGlobal=%s, using=%s}".formatted(isLocal, isParameter, isGlobal, using);
	}
}
