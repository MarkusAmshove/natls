package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.ISqrtOperandNode;

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
