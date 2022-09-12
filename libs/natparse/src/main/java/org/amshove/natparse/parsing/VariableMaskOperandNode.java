package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IVariableMaskOperandNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class VariableMaskOperandNode extends BaseSyntaxNode implements IVariableMaskOperandNode
{
	private IVariableReferenceNode variableMask;

	@Override
	public IVariableReferenceNode variableMask()
	{
		return variableMask;
	}

	void setVariableMask(IVariableReferenceNode node)
	{
		variableMask = node;
	}
}
