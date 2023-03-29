package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IAssignmentStatementNode;
import org.amshove.natparse.natural.IOperandNode;

class AssignmentStatementNode extends StatementNode implements IAssignmentStatementNode
{
	private IOperandNode target;
	private IOperandNode operand;

	@Override
	public IOperandNode target()
	{
		return target;
	}

	@Override
	public IOperandNode operand()
	{
		return operand;
	}

	void setTarget(IOperandNode target)
	{
		this.target = target;
	}

	void setOperand(IOperandNode operand)
	{
		this.operand = operand;
	}

	@Override
	public ReadOnlyList<IOperandNode> mutations()
	{
		return ReadOnlyList.of(target);
	}
}
