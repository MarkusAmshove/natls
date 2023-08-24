package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IDecideOnBranchNode;
import org.amshove.natparse.natural.IOperandNode;

import java.util.ArrayList;
import java.util.List;

class DecideOnBranchNode extends StatementWithBodyNode implements IDecideOnBranchNode
{
	private final List<IOperandNode> values = new ArrayList<>();
	private boolean hasValueRange;

	@Override
	public ReadOnlyList<IOperandNode> values()
	{
		return ReadOnlyList.from(values);
	}

	@Override
	public boolean hasValueRange()
	{
		return hasValueRange;
	}

	void addOperand(IOperandNode operand)
	{
		values.add(operand);
	}

	void setHasValueRange()
	{
		this.hasValueRange = true;
	}
}
