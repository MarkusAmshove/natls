package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.ICallnatNode;

class CallnatNode extends StatementNode implements ICallnatNode
{
	private SyntaxToken calledModule;

	@Override
	public SyntaxToken calledModule()
	{
		return calledModule;
	}

	void setCalledModule(SyntaxToken calledModule)
	{
		this.calledModule = calledModule;
	}
}
