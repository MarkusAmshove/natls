package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IRedefinitionNode;
import org.amshove.natparse.natural.IVariableNode;

public class RedefinitionNode extends GroupNode implements IRedefinitionNode
{
	private IVariableNode targetNode;

	public RedefinitionNode(VariableNode variable)
	{
		super(variable);
	}

	@Override
	public IVariableNode target()
	{
		return targetNode;
	}

	void setTarget(IVariableNode targetNode)
	{
		this.targetNode = targetNode;
	}
}
