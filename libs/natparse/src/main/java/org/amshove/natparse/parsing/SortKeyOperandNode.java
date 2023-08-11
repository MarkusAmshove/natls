package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.ISortKeyOperandNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class SortKeyOperandNode extends BaseSyntaxNode implements ISortKeyOperandNode
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
