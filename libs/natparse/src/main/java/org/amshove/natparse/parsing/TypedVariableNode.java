package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.ITypedNode;
import org.amshove.natparse.natural.IVariableTypeNode;

class TypedVariableNode extends VariableNode implements ITypedNode
{
	private VariableType type;

	public TypedVariableNode(VariableNode variable)
	{
		setLevel(variable.level());
		setDeclaration(variable.declaration());
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

	void setType(VariableType type)
	{
		this.type = type;
	}
}
