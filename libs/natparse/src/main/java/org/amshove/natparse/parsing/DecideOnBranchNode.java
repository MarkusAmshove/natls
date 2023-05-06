package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IDecideOnBranchNode;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.IStatementListNode;

import java.util.ArrayList;
import java.util.List;

class DecideOnBranchNode extends BaseSyntaxNode implements IDecideOnBranchNode
{
	private final List<IOperandNode> values = new ArrayList<>();
	private IStatementListNode body;

	@Override
	public ReadOnlyList<IOperandNode> values()
	{
		return ReadOnlyList.from(values);
	}

	@Override
	public IStatementListNode body()
	{
		return body;
	}

	void addOperand(IOperandNode operand)
	{
		values.add(operand);
	}

	void setBody(IStatementListNode body)
	{
		this.body = body;
	}
}
