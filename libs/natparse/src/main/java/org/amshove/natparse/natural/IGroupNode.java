package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

import javax.annotation.Nullable;

public interface IGroupNode extends IVariableNode
{
	ReadOnlyList<IVariableNode> variables();

	@Nullable
	IVariableNode findVariable(String name);
}
