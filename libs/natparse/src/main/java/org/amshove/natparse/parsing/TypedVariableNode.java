package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IArrayDimension;
import org.amshove.natparse.natural.ITypedVariableNode;
import org.amshove.natparse.natural.IVariableType;

class TypedVariableNode extends VariableNode implements ITypedVariableNode
{
	private VariableType type;

	public TypedVariableNode(VariableNode variable)
	{
		setLevel(variable.level());
		setDeclaration(variable.identifierNode());
		setScope(variable.scope());
		for (var dimension : variable.dimensions())
		{
			addDimension((ArrayDimension) dimension);
		}
		for (var node : variable.descendants())
		{
			addNode((BaseSyntaxNode) node);
		}
	}

	@Override
	public IVariableType type()
	{
		return type;
	}

	void setType(VariableType type)
	{
		this.type = type;
	}

	@Override
	void inheritDimensions(ReadOnlyList<IArrayDimension> dimensions)
	{
		for (var dimension : dimensions)
		{
			if (this.dimensions.contains(dimension))
			{
				continue;
			}

			if (dimension.isUpperVariable())
			{
				var boundDimension = new ArrayDimension();
				boundDimension.setLowerBound(dimension.lowerBound());
				boundDimension.setUpperBound(type.byteSize());
				this.dimensions.add(0, boundDimension);
			}
			else
			{
				this.dimensions.add(0, dimension);
			}
		}
	}

	@Override
	void addDimension(ArrayDimension dimension)
	{
		addNode(dimension);
		if (dimension.isUpperVariable())
		{
			if (type == null)
			{
				// Will be evaluated once typed
				return;
			}
			var boundDimension = new ArrayDimension();
			boundDimension.setLowerBound(dimension.lowerBound());
			boundDimension.setUpperBound(type.byteSize());
			dimensions.add(boundDimension);
		}
		else
		{
			dimensions.add(dimension);
		}
	}
}
