package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IDecideOnBranchNode;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.IStatementListNode;

class DecideOnBranchNode extends BaseSyntaxNode implements IDecideOnBranchNode
{
	private IOperandNode operand;
	private IStatementListNode body;

	@Override
	public IOperandNode operand()
	{
		return operand;
	}

	@Override
	public IStatementListNode body()
	{
		return body;
	}

	void setOperand(IOperandNode operand)
	{
		this.operand = operand;
	}

	void setBody(IStatementListNode body)
	{
		this.body = body;
	}
}
