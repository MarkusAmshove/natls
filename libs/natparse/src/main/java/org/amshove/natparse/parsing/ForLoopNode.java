package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IForLoopNode;
import org.amshove.natparse.natural.IVariableReferenceNode;
import org.amshove.natparse.natural.IOperandNode;

class ForLoopNode extends StatementWithBodyNode implements IForLoopNode
{
	private IVariableReferenceNode loopControl;
	private IOperandNode upperBound;

	@Override
	public IVariableReferenceNode loopControl()
	{
		return loopControl;
	}

	@Override
	public IOperandNode upperBound()
	{
		return upperBound;
	}

	void setUpperBound(IOperandNode operandNode)
	{
		this.upperBound = operandNode;
	}

	void setLoopControl(IVariableReferenceNode loopControl)
	{
		this.loopControl = loopControl;
	}

	@Override
	public ReadOnlyList<IOperandNode> mutations()
	{
		return ReadOnlyList.of(loopControl);
	}
}
