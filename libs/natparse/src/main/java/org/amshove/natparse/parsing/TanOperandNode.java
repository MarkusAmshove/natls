package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.ITanOperandNode;
import org.amshove.natparse.natural.IOperandNode;

class TanOperandNode extends BaseSyntaxNode implements ITanOperandNode
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
