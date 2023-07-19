package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.ISinOperandNode;
import org.amshove.natparse.natural.IOperandNode;

class SinOperandNode extends BaseSyntaxNode implements ISinOperandNode
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
