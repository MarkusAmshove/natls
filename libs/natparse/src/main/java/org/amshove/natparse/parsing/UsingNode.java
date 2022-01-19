package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IDefineData;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.IUsingNode;
import org.amshove.natparse.natural.VariableScope;

class UsingNode extends BaseSyntaxNode implements IUsingNode
{
	private SyntaxToken using;
	private VariableScope scope;
	private IDefineData defineData;
	private INaturalModule referencingModule;

	@Override
	public SyntaxToken target()
	{
		return using;
	}

	@Override
	public boolean isLocalUsing()
	{
		return scope.isLocal();
	}

	@Override
	public boolean isGlobalUsing()
	{
		return scope.isGlobal();
	}

	@Override
	public boolean isParameterUsing()
	{
		return scope.isParameter();
	}

	@Override
	public IDefineData defineData()
	{
		return defineData;
	}

	void setUsingTarget(SyntaxToken using)
	{
		this.using = using;
	}

	void setScope(SyntaxKind scopeKind)
	{
		this.scope = VariableScope.fromSyntaxKind(scopeKind);
	}

	void setDefineData(IDefineData defineData)
	{
		this.defineData = defineData;
	}

	@Override
	public String toString()
	{
		return "UsingNode{scope=%s, using=%s}".formatted(scope, using);
	}

	@Override
	public INaturalModule reference()
	{
		return referencingModule;
	}

	@Override
	public SyntaxToken referencingToken()
	{
		return using;
	}

	void setReferencingModule(NaturalModule module)
	{
		referencingModule = module;
		if (referencingModule != null)
		{
			module.addReference(this);
		}
	}
}
