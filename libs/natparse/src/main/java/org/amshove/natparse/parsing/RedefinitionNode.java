package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IRedefinitionNode;
import org.amshove.natparse.natural.IReferencableNode;
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
		targetNode.addReference(this);
	}

	void addFillerBytes(int bytes)
	{
		fillerBytes += bytes;
	}

	@Override
	public IReferencableNode reference()
	{
		return targetNode;
	}

	@Override
	public SyntaxToken referencingToken()
	{
		return declaration();
	}

	@Override
	public SyntaxToken token()
	{
		return declaration();
	}
}
