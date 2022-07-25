package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public interface IVariableNode extends IReferencableNode
{
	// TODO: Might need "effectiveScope"? Beacuse when LOCAL USING a PDA this returns parameter
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
