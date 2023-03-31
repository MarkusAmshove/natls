package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.IValOperandNode;

class ValOperandNode extends BaseSyntaxNode implements IValOperandNode
{
	private IOperandNode operand;

	@Override
	public IOperandNode operand()
	{
		return operand;
	}

	void setVariable(IOperandNode operand)
	{
		this.operand = operand;
	}
}
