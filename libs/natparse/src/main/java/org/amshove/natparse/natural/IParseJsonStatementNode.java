package org.amshove.natparse.natural;

import org.jspecify.annotations.Nullable;

public interface IParseJsonStatementNode extends IStatementWithBodyNode, IMutateVariables, ILabelReferencable
{
	IOperandNode jsonDocument();

	@Nullable
	IOperandNode codePage();

	@Nullable
	IOperandNode jsonPath();

	@Nullable
	IOperandNode jsonPathSeparator();

	@Nullable
	IOperandNode jsonAttributeName();

	@Nullable
	IOperandNode jsonAttributeValue();

	@Nullable
	IOperandNode giving();

	@Nullable
	IOperandNode subcode();
}
