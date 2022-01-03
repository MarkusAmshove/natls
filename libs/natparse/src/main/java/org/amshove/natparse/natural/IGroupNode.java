package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface IGroupNode extends IVariableNode
{
	ReadOnlyList<IVariableNode> variables();
}
