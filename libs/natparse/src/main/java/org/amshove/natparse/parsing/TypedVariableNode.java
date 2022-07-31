package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.ITypedVariableNode;
import org.amshove.natparse.natural.IVariableType;

class TypedVariableNode extends VariableNode implements ITypedVariableNode
{
	private VariableType type;

	public TypedVariableNode(VariableNode variable)
	{
		setLevel(variable.level());
		setDeclaration(variable.declaration());
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
}
