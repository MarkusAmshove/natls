package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IIntOperandNode;
import org.amshove.natparse.natural.IOperandNode;

class IntOperandNode extends BaseSyntaxNode implements IIntOperandNode
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
