package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IAbsOperandNode;
import org.amshove.natparse.natural.IOperandNode;

class AbsOperandNode extends BaseSyntaxNode implements IAbsOperandNode
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
