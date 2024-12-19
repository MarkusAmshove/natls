package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

import javax.annotation.Nullable;

public interface IGroupNode extends IVariableNode
{
	ReadOnlyList<IVariableNode> variables();

	/**
	 * Returns child variables flattened, meaning that all {@link IGroupNode} get expanded and their child variables
	 * included.</br>
	 * {@link IGroupNode}s themselves are not included.
	 **/
	ReadOnlyList<IVariableNode> flattenVariables();

	@Nullable
	IVariableNode findVariable(String name);
}
