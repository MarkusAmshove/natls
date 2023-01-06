package org.amshove.natparse.natural;

import java.util.Optional;

public interface ISkipStatementNode extends IStatementNode
{
	IOperandNode toSkip();

	Optional<IReportSpecificationOperandNode> reportSpecification();
}
