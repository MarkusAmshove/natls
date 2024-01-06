package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface IWriteNode extends IStatementNode, ICanHaveReportSpecification
{
	ReadOnlyList<IAttributeNode> statementAttributes();

	ReadOnlyList<IOutputElementNode> operands();
}
