package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IRedefinitionNode;
import org.amshove.natparse.natural.IVariableNode;

class RedefinitionNode extends GroupNode implements IRedefinitionNode
{
	private IVariableNode targetNode;
	private int fillerBytes;

	public RedefinitionNode(VariableNode variable)
	{
		super(variable);
	}

	@Override
	public IVariableNode target()
	{
		return targetNode;
	}

	@Override
	public int fillerBytes()
	{
		return fillerBytes;
	}

	void setTarget(IVariableNode targetNode)
	{
		this.targetNode = targetNode;
	}

	void addFillerBytes(int bytes)
	{
		fillerBytes += bytes;
	}
}
