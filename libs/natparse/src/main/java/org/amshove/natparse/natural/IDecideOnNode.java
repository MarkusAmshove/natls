package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

import javax.annotation.Nullable;

public interface IDecideOnNode extends IStatementNode
{
	IOperandNode operand();

	ReadOnlyList<IDecideOnBranchNode> branches();

	@Nullable
	IStatementListNode anyValue();

	@Nullable
	IStatementListNode allValues();

	IStatementListNode noneValue();
}
