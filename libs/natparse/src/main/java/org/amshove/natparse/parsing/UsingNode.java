package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.*;

class UsingNode extends BaseSyntaxNode implements IUsingNode
{
	private SyntaxToken using;
	private SyntaxToken withBlock;
	private VariableScope scope;
	private IDefineData defineData;
	private INaturalModule referencingModule;

	@Override
	public SyntaxToken target()
	{
		return using;
	}

	@Override
	public SyntaxToken withBlock()
	{
		return withBlock;
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

	@Override
	public VariableScope scope()
	{
		return scope;
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

	void setWithBlock(SyntaxToken withBlock)
	{
		this.withBlock = withBlock;
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

	@Override
	public ReadOnlyList<IOperandNode> providedParameter()
	{
		return ReadOnlyList.empty();
	}

	void setReferencingModule(INaturalModule module)
	{
		referencingModule = module;
		if (referencingModule != null)
		{
			module.addCaller(this);
		}
	}
}
