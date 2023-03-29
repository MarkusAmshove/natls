package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IMutateVariables;
import org.amshove.natparse.natural.IOperandNode;

import java.util.ArrayList;
import java.util.List;

class BasicMathStatementNode extends StatementNode implements IMutateVariables
{
	private IOperandNode target;
	private boolean isRounded;
	private boolean isGiving;
	private final List<IOperandNode> operands = new ArrayList<>();

	public IOperandNode target()
	{
		return target;
	}

	public ReadOnlyList<IOperandNode> operands()
	{
		return ReadOnlyList.from(operands);
	}

	public boolean isRounded()
	{
		return isRounded;
	}

	public boolean isGiving()
	{
		return isGiving;
	}

	void setTarget(IOperandNode target)
	{
		this.target = target;
	}

	void addOperand(IOperandNode operand)
	{
		operands.add(operand);
	}

	void setIsGiving(boolean isGiving)
	{
		this.isGiving = isGiving;
	}

	void setIsRounded(boolean isRounded)
	{
		this.isRounded = isRounded;
	}

	@Override
	public ReadOnlyList<IOperandNode> mutations()
	{
		return ReadOnlyList.of(target);
	}
}
