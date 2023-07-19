package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IMinOperandNode;
import org.amshove.natparse.natural.IOperandNode;

class MinOperandNode extends BaseSyntaxNode implements IMinOperandNode
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
