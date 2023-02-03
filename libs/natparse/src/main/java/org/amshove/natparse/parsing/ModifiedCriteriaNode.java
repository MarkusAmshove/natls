package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.conditionals.IModifiedCriteriaNode;

class ModifiedCriteriaNode extends BaseSyntaxNode implements IModifiedCriteriaNode
{
	private IOperandNode operand;
	private boolean isNotModified;

	@Override
	public IOperandNode operand()
	{
		return operand;
	}

	@Override
	public boolean isNotModified()
	{
		return isNotModified;
	}

	void setOperand(IOperandNode operand)
	{
		this.operand = operand;
	}

	void setIsNotModified(boolean isNotModified)
	{
		this.isNotModified = isNotModified;
	}
}
