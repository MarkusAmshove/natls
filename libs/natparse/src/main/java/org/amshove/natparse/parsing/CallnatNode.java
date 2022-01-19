package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.ICallnatNode;
import org.amshove.natparse.natural.INaturalModule;

class CallnatNode extends StatementNode implements ICallnatNode
{
	private SyntaxToken calledModule;
	private INaturalModule referencedModule;

	@Override
	public SyntaxToken calledModule()
	{
		return calledModule;
	}

	void setCalledModule(SyntaxToken calledModule)
	{
		this.calledModule = calledModule;
	}

	@Override
	public INaturalModule reference()
	{
		return referencedModule;
	}

	@Override
	public SyntaxToken referencingToken()
	{
		return calledModule;
	}

	void setReferencedModule(NaturalModule module)
	{
		this.referencedModule = module;
		if (module != null)
		{
			module.addReference(this);
		}
	}
}
