package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IExamineNode;
import org.amshove.natparse.natural.IOperandNode;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

class ExamineNode extends StatementNode implements IExamineNode
{
	private IOperandNode examined;
	private final List<IOperandNode> givingIndex = new ArrayList<>();
	private IOperandNode givingNumber;
	private IOperandNode givingPosition;
	private IOperandNode givingLength;

	@Override
	public IOperandNode examined()
	{
		return examined;
	}

	@Override
	public @Nullable IOperandNode givingNumber()
	{
		return givingNumber;
	}

	@Override
	public @Nullable IOperandNode givingPosition()
	{
		return givingPosition;
	}

	@Override
	public @Nullable IOperandNode givingLength()
	{
		return givingLength;
	}

	@Override
	public ReadOnlyList<IOperandNode> givingIndex()
	{
		return ReadOnlyList.from(givingIndex);
	}

	void setExamined(IOperandNode examined)
	{
		this.examined = examined;
	}

	void setGivingNumber(IOperandNode givingNumber)
	{
		this.givingNumber = givingNumber;
	}

	void setGivingPosition(IOperandNode givingPosition)
	{
		this.givingPosition = givingPosition;
	}

	void setGivingLength(IOperandNode givingLength)
	{
		this.givingLength = givingLength;
	}

	void addGivingIndex(IOperandNode givingIndex)
	{
		this.givingIndex.add(givingIndex);
	}
}
