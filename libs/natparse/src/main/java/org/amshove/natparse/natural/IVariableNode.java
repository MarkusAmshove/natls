package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;
import java.util.*;
import java.util.stream.*;

public non-sealed interface IVariableNode extends IReferencableNode, IParameterDefinitionNode
{
	// TODO: Might need "effectiveScope"? Beacuse when LOCAL USING a PDA this returns parameter
	VariableScope scope();

	int level();

	String name();

	String qualifiedName();

	ITokenNode identifierNode();

	ReadOnlyList<IArrayDimension> dimensions();

	boolean isInView();

	default boolean isArray()
	{
		var dimensions = dimensions();
		return dimensions != null && !dimensions.isEmpty();
	}

	/**
	 * Returns a list of all parent groups in descending (by level) order.
	 **/
	default ReadOnlyList<IGroupNode> getVariableParentsDescending()
	{
		if (level() == 1)
		{
			return ReadOnlyList.empty();
		}

		var parents = new ArrayList<IGroupNode>();

		var current = parent();
		while (current != null)
		{
			if (current instanceof IGroupNode group)
			{
				parents.add(group);
			}

			current = current.parent();
		}

		return ReadOnlyList.from(parents);
	}

	/**
	 * Returns a list of all parent groups in ascending (by level) order.
	 **/
	default ReadOnlyList<IGroupNode> getVariableParentsAscending()
	{
		if (level() == 1)
		{
			return ReadOnlyList.empty();
		}

		var parents = getVariableParentsDescending();
		return parents.reverse();
	}

	/**
	 * Returns a formatted list of array dimensions without parens.<br>
	 * Example: `1:*,1:5`
	 */
	default String formatDimensionList()
	{
		if (!isArray())
		{
			return "";
		}

		return dimensions().stream()
			.map(IArrayDimension::displayFormat)
			.collect(Collectors.joining(","));
	}
}
