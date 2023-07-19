package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.ICountOperandNode;
import org.amshove.natparse.natural.IOperandNode;

class CountOperandNode extends BaseSyntaxNode implements ICountOperandNode
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
