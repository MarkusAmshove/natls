package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.conditionals.ISpecifiedCriteriaNode;

class SpecifiedCriteriaNode extends BaseSyntaxNode implements ISpecifiedCriteriaNode
{
	private IOperandNode operand;
	private boolean isNotSpecified;

	@Override
	public IOperandNode operand()
	{
		return operand;
	}

	@Override
	public boolean isNotSpecified()
	{
		return isNotSpecified;
	}

	void setOperand(IOperandNode operand)
	{
		this.operand = operand;
	}

	void setIsNotSpecified(boolean isNotSpecified)
	{
		this.isNotSpecified = isNotSpecified;
	}
}
