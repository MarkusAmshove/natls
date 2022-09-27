package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IModuleReferencingNode;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.IOperandNode;

import java.util.ArrayList;
import java.util.List;

class ModuleReferencingNode extends StatementNode implements IModuleReferencingNode
{
	private SyntaxToken calledModule;
	private INaturalModule referencedModule;
	private List<IOperandNode> providedParameter = new ArrayList<>();

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

	@Override
	public ReadOnlyList<IOperandNode> providedParameter()
	{
		return ReadOnlyList.from(providedParameter);
	}

	void addParameter(IOperandNode parameter)
	{
		providedParameter.add(parameter);
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
