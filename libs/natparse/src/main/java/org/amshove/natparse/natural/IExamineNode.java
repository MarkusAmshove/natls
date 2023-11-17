package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

import javax.annotation.Nullable;

public interface IExamineNode extends IStatementNode
{
	IOperandNode examined();

	@Nullable
	IOperandNode givingNumber();

	@Nullable
	IOperandNode givingPosition();

	@Nullable
	IOperandNode givingLength();

	ReadOnlyList<IOperandNode> givingIndex();
}
