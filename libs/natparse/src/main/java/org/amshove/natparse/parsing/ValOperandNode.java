package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IValOperandNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class ValOperandNode extends BaseSyntaxNode implements IValOperandNode
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
