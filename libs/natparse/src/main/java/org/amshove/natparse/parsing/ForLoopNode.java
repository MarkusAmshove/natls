package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IForLoopNode;
import org.amshove.natparse.natural.IOperandNode;

class ForLoopNode extends StatementWithBodyNode implements IForLoopNode
{
	private IOperandNode upperBound;

	@Override
	public IOperandNode upperBound()
	{
		return upperBound;
	}

	void setUpperBound(IOperandNode operandNode)
	{
		this.upperBound = operandNode;
	}
}
