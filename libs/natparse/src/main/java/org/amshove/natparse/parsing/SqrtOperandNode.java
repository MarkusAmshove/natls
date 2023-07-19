package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.ISqrtOperandNode;
import org.amshove.natparse.natural.IOperandNode;

class SqrtOperandNode extends BaseSyntaxNode implements ISqrtOperandNode
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
