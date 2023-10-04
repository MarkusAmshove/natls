package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.conditionals.IIfBreakCriteriaNode;

class IfBreakCriteriaNode extends BaseSyntaxNode implements IIfBreakCriteriaNode
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
