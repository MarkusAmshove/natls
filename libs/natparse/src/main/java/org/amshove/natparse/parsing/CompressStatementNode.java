package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.ICompressStatementNode;
import org.amshove.natparse.natural.IOperandNode;

import java.util.ArrayList;
import java.util.List;

class CompressStatementNode extends StatementNode implements ICompressStatementNode
{
	private IOperandNode intoTarget;
	private List<IOperandNode> operands = new ArrayList<>();
	private boolean isNumeric;
	private boolean isFull;
	private boolean isLeavingSpace = true;
	private boolean isWithDelimiters;
	private boolean isWithAllDelimiters;
	private IOperandNode delimiter;

	@Override
	public IOperandNode intoTarget()
	{
		return intoTarget;
	}

	@Override
	public List<IOperandNode> operands()
	{
		return operands;
	}

	@Override
	public boolean isNumeric()
	{
		return isNumeric;
	}

	@Override
	public boolean isFull()
	{
		return isFull;
	}

	@Override
	public boolean isLeavingSpace()
	{
		return isLeavingSpace;
	}

	@Override
	public boolean isWithDelimiters()
	{
		return isWithDelimiters;
	}

	@Override
	public boolean isWithAllDelimiters()
	{
		return isWithAllDelimiters;
	}

	@Override
	public IOperandNode delimiter()
	{
		return delimiter;
	}

	void setIntoTarget(IOperandNode intoTarget)
	{
		this.intoTarget = intoTarget;
	}

	void setNumeric(boolean numeric)
	{
		isNumeric = numeric;
	}

	void setFull(boolean full)
	{
		isFull = full;
	}

	void setLeavingSpace(boolean leavingSpace)
	{
		isLeavingSpace = leavingSpace;
	}

	void setWithDelimiters(boolean withDelimiters)
	{
		isWithDelimiters = withDelimiters;
	}

	void setWithAllDelimiters(boolean withAllDelimiters)
	{
		isWithAllDelimiters = withAllDelimiters;
	}

	void addOperand(IOperandNode consumeOperandNode)
	{
		operands.add(consumeOperandNode);
	}

	void setDelimiter(IOperandNode delimiter)
	{
		this.delimiter = delimiter;
	}
}
