package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.IScanOperandNode;

class ScanOperandNode extends BaseSyntaxNode implements IScanOperandNode
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
