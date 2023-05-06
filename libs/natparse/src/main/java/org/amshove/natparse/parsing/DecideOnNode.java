package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IDecideOnBranchNode;
import org.amshove.natparse.natural.IDecideOnNode;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.IStatementListNode;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

class DecideOnNode extends StatementNode implements IDecideOnNode
{
	private IOperandNode operand;
	private IStatementListNode noneValue;
	private IStatementListNode anyValue;
	private IStatementListNode allValues;
	private final List<IDecideOnBranchNode> branches = new ArrayList<>();

	@Override
	public IOperandNode operand()
	{
		return operand;
	}

	@Override
	public ReadOnlyList<IDecideOnBranchNode> branches()
	{
		return ReadOnlyList.from(branches);
	}

	@Nullable
	@Override
	public IStatementListNode anyValue()
	{
		return anyValue;
	}

	@Nullable
	@Override
	public IStatementListNode allValues()
	{
		return allValues;
	}

	@Override
	public IStatementListNode noneValue()
	{
		return noneValue;
	}

	void setAllValues(IStatementListNode allValues)
	{
		this.allValues = allValues;
	}

	void setAnyValue(IStatementListNode anyValue)
	{
		this.anyValue = anyValue;
	}

	void setNoneValue(IStatementListNode noneValue)
	{
		this.noneValue = noneValue;
	}

	void setOperand(IOperandNode operand)
	{
		this.operand = operand;
	}

	void addBranch(IDecideOnBranchNode branch)
	{
		branches.add(branch);
	}
}
