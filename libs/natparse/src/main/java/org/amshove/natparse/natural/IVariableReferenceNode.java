package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface IVariableReferenceNode extends ISymbolReferenceNode, IOperandNode
{
	ReadOnlyList<IOperandNode> dimensions();
}
