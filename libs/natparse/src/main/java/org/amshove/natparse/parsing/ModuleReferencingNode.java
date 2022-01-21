package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IModuleReferencingNode;
import org.amshove.natparse.natural.INaturalModule;

class ModuleReferencingNode extends StatementNode implements IModuleReferencingNode
{
	private SyntaxToken calledModule;
	private INaturalModule referencedModule;

	void setReferencingToken(SyntaxToken calledModule)
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
