package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IFracOperandNode;
import org.amshove.natparse.natural.IMathFunctionOperandNode;
import org.amshove.natparse.natural.IOperandNode;

class FracOperandNode extends BaseSyntaxNode implements IFracOperandNode
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
