package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.ICosOperandNode;
import org.amshove.natparse.natural.IOperandNode;

class CosOperandNode extends BaseSyntaxNode implements ICosOperandNode
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
