package org.amshove.natparse.natural;

import org.jspecify.annotations.Nullable;

public interface IParseXmlStatementNode extends IStatementWithBodyNode, IMutateVariables, ILabelReferencable
{
	IOperandNode xmlDocument();

	@Nullable
	IOperandNode xmlElementPath();

	@Nullable
	IOperandNode xmlElementName();

	@Nullable
	IOperandNode xmlElementValue();

	@Nullable
	IOperandNode xmlNamespace();

	@Nullable
	IOperandNode xmlPrefix();
}
