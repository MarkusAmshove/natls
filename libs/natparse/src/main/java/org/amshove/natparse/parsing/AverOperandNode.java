package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IAverOperandNode;
import org.amshove.natparse.natural.IOperandNode;

class AverOperandNode extends BaseSyntaxNode implements IAverOperandNode
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
