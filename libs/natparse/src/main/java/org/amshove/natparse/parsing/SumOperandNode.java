package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.ISumOperandNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class SumOperandNode extends BaseSyntaxNode implements ISumOperandNode
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
