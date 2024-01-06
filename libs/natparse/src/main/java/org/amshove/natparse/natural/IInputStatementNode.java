package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface IInputStatementNode extends IStatementNode
{
	ReadOnlyList<IOutputElementNode> operands();

	ReadOnlyList<IAttributeNode> statementAttributes();
}
