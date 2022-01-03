package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.ITypedVariableNode;
import org.amshove.natparse.natural.IVariableTypeNode;

class TypedVariableNode extends VariableNode implements ITypedVariableNode
{
	private VariableTypeNode type;

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
	public IVariableTypeNode type()
	{
		return type;
	}

	void setType(VariableTypeNode type)
	{
		this.type = type;
	}
}
