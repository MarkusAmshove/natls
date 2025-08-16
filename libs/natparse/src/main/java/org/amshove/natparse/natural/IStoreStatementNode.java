package org.amshove.natparse.natural;

import org.jspecify.annotations.Nullable;

public interface IStoreStatementNode extends IStatementNode, IAdabasAccessStatementNode
{
	@Nullable
	IOperandNode password();

	@Nullable
	IOperandNode cipher();
}
