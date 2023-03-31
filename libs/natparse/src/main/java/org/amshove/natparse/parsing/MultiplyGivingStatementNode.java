package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IMultiplyGivingStatementNode;
import org.amshove.natparse.natural.IOperandNode;

class MultiplyGivingStatementNode extends BasicMathStatementNode implements IMultiplyGivingStatementNode
{
	private IOperandNode giving;

	MultiplyGivingStatementNode(MultiplyStatementNode multiply)
	{
		setIsGiving(true);
		setTarget(multiply.target());
		setIsRounded(multiply.isRounded());
		setParent(multiply.parent());
		for (var operand : multiply.operands())
		{
			addOperand(operand);
		}
		copyFrom(multiply);
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
		return ReadOnlyList.of(giving);
	}
}
