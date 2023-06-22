package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IOldOperandNode;
import org.amshove.natparse.natural.IOperandNode;

class OldOperandNode extends BaseSyntaxNode implements IOldOperandNode
{
	private IOperandNode operand;

	@Override
	public IOperandNode operand()
	{
		return operand;
	}

	void setOperand(IOperandNode operand)
	{
		this.operand = operand;
	}
}
