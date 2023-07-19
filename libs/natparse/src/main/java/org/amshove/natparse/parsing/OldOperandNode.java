package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IOldOperandNode;
import org.amshove.natparse.natural.IOperandNode;

class OldOperandNode extends BaseSyntaxNode implements IOldOperandNode
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
