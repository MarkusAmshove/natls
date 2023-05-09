package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.ICountOperandNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class CountOperandNode extends BaseSyntaxNode implements ICountOperandNode
{
	private IVariableReferenceNode variable;

	@Override
	public IVariableReferenceNode variable()
	{
		return variable;
	}

	void setVariable(IVariableReferenceNode variable)
	{
		this.variable = variable;
	}
}
