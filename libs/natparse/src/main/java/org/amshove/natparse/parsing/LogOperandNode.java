package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.ILogOperandNode;
import org.amshove.natparse.natural.IOperandNode;

class LogOperandNode extends BaseSyntaxNode implements ILogOperandNode
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
