package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IIntOperandNode;
import org.amshove.natparse.natural.IOldOperandNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class OldOperandNode extends BaseSyntaxNode implements IOldOperandNode
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
