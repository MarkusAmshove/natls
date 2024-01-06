package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface IInputStatementNode extends IStatementNode
{
	ReadOnlyList<IOutputOperandNode> operands();

	ReadOnlyList<IAttributeNode> statementAttributes();
}
