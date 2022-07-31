package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

public non-sealed interface IVariableNode extends IReferencableNode, IParameterDefinitionNode
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
