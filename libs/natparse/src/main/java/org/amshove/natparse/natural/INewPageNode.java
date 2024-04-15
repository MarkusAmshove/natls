package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.output.IOutputElementNode;

public interface INewPageNode extends IStatementNode, ICanHaveReportSpecification
{
	ReadOnlyList<IOutputElementNode> operands();
}
