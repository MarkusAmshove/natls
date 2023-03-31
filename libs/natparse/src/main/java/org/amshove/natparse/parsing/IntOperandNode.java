package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IIntOperandNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class IntOperandNode extends BaseSyntaxNode implements IIntOperandNode
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
