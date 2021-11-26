package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface IScopeNode extends ISyntaxNode
{
	VariableScope scope();
	ReadOnlyList<IVariableNode> variables();
}
