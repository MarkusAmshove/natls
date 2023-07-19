package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IAtnOperandNode;
import org.amshove.natparse.natural.IOperandNode;

class AtnOperandNode extends BaseSyntaxNode implements IAtnOperandNode
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
