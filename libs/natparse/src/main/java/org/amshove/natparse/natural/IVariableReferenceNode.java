package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface IVariableReferenceNode extends ISymbolReferenceNode, IOperandNode, ITypeInferable
{
	ReadOnlyList<IOperandNode> dimensions();
}
