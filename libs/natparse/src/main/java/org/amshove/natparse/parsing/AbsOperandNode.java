package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IAbsOperandNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class AbsOperandNode extends BaseSyntaxNode implements IAbsOperandNode
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
