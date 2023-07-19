package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IMaxOperandNode;
import org.amshove.natparse.natural.IOperandNode;

class MaxOperandNode extends BaseSyntaxNode implements IMaxOperandNode
{
	private IOperandNode parameter;

	@Override
	public IOperandNode parameter()
	{
		return parameter;
	}

	void setParameter(IOperandNode parameter)
	{
		this.parameter = parameter;
	}
}
