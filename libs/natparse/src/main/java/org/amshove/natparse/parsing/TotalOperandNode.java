package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IMathFunctionOperandNode;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.ITotalOperandNode;

class TotalOperandNode extends BaseSyntaxNode implements ITotalOperandNode
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
