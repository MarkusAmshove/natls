package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IAdabasIndexAccess;
import org.amshove.natparse.natural.IOperandNode;

import java.util.ArrayList;
import java.util.List;

class AdabasIndexAccess extends BaseSyntaxNode implements IAdabasIndexAccess
{
	private final List<IOperandNode> operands = new ArrayList<>();

	@Override
	public ReadOnlyList<IOperandNode> operands()
	{
		return ReadOnlyList.from(operands);
	}

	void addOperand(IOperandNode operand)
	{
		addNode((BaseSyntaxNode) operand);
		operands.add(operand);
	}
}
