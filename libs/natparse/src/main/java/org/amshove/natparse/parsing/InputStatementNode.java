package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IInputStatementNode;
import org.amshove.natparse.natural.IOperandNode;

import java.util.ArrayList;
import java.util.List;

class InputStatementNode extends StatementNode implements IInputStatementNode
{
	private final List<IOperandNode> operands = new ArrayList<>();

	@Override
	public ReadOnlyList<IOperandNode> operands()
	{
		return ReadOnlyList.from(operands);
	}

	void addOperand(IOperandNode operand)
	{
		if (operand == null)
		{
			// stuff like tab setting, line skip etc.
			return;
		}

		operands.add(operand);
	}

	@Override
	public ReadOnlyList<IOperandNode> mutations()
	{
		// Ideally, should check for (AD=M) on statement level and field level. For now, every var is mutable.
		return ReadOnlyList.from(operands);
	}
}
