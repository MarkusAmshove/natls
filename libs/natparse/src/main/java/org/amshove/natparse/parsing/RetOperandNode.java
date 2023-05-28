package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.ILiteralNode;
import org.amshove.natparse.natural.IRetOperandNode;

class RetOperandNode extends BaseSyntaxNode implements IRetOperandNode
{
	private ILiteralNode parameter;

	@Override
	public ILiteralNode parameter()
	{
		return parameter;
	}

	void setParameter(ILiteralNode parameter)
	{
		this.parameter = parameter;
	}
}
