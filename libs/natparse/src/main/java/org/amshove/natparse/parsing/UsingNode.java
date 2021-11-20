package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IUsingNode;

class UsingNode extends BaseSyntaxNode implements IUsingNode
{
	private boolean isLocal = false;
	private boolean isParameter = false;
	private boolean isGlobal = false;
	private SyntaxToken using;

	@Override
	public SyntaxToken using()
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

	void setUsing(SyntaxToken using)
	{
		this.using = using;
	}

	void setLocal()
	{
		isLocal = true;
		isParameter = false;
		isGlobal = false;
	}

	void setParameter()
	{
		isLocal = false;
		isParameter = true;
		isGlobal = false;
	}

	void setGlobal()
	{
		isLocal = false;
		isParameter = false;
		isGlobal = true;
	}
}
