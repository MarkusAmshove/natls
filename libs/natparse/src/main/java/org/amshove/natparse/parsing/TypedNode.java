package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.ITypedNode;
import org.amshove.natparse.natural.IVariableType;

public class TypedNode extends VariableNode implements ITypedNode
{
	private VariableType type;

	public TypedNode(VariableNode variable)
	{
		setLevel(variable.level());
		setDeclaration(variable.declaration());
		for (var node : variable.nodes())
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
