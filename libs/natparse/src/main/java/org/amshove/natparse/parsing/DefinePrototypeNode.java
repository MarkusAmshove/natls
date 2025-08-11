package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IDefinePrototypeNode;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

class DefinePrototypeNode extends StatementNode implements IDefinePrototypeNode
{

	private SyntaxToken prototypeName;
	private IVariableReferenceNode variableReference;

	@Override
	public SyntaxToken nameToken()
	{
		return variableReference != null ? variableReference.referencingToken() : prototypeName;
	}

	@Override
	public boolean isVariable()
	{
		return variableReference != null;
	}

	@Nullable
	@Override
	public IVariableReferenceNode variableReference()
	{
		return variableReference;
	}

	void setPrototype(SyntaxToken token)
	{
		prototypeName = token;
	}

	void setVariableReference(IVariableReferenceNode reference)
	{
		this.variableReference = reference;
	}
}
