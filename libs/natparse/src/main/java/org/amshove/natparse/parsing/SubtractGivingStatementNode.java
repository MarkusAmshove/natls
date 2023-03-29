package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.ISubtractGivingStatementNode;

class SubtractGivingStatementNode extends BasicMathStatementNode implements ISubtractGivingStatementNode
{
	private IOperandNode giving;

	SubtractGivingStatementNode(SubtractStatementNode subtract)
	{
		setIsGiving(true);
		setTarget(subtract.target());
		setIsRounded(subtract.isRounded());
		setParent(subtract.parent());
		for (var operand : subtract.operands())
		{
			addOperand(operand);
		}
		copyFrom(subtract);
	}

	@Override
	public IOperandNode giving()
	{
		return giving;
	}

	void setGiving(IOperandNode giving)
	{
		this.giving = giving;
	}

	@Override
	public ReadOnlyList<IOperandNode> mutations()
	{
		return ReadOnlyList.of(target(), giving);
	}
}
