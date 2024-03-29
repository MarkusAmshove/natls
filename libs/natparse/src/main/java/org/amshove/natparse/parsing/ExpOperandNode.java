package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IExpOperandNode;
import org.amshove.natparse.natural.IOperandNode;

class ExpOperandNode extends BaseSyntaxNode implements IExpOperandNode
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
