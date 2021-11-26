package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface IVariableNode extends ISymbolNode
{
	VariableScope scope();
	int level();
	String name();
	String qualifiedName();
	ReadOnlyList<IArrayDimension> dimensions();

	default boolean isArray()
	{
		return dimensions() != null && dimensions().size() > 0;
	}
}
