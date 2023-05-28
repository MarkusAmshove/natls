package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.ITotalOperandNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class TotalOperandNode extends BaseSyntaxNode implements ITotalOperandNode
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
