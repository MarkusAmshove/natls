package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.output.IOutputElementNode;

public interface IInputStatementNode extends IStatementNode
{
	ReadOnlyList<IOutputElementNode> operands();

	ReadOnlyList<IAttributeNode> statementAttributes();
}
