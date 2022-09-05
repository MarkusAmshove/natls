package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IPosNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class PosNode extends BaseSyntaxNode implements IPosNode
{
	private IVariableReferenceNode positionOf;

	@Override
	public IVariableReferenceNode positionOf()
	{
		return positionOf;
	}

	void setPositionOf(IVariableReferenceNode positionOf)
	{
		this.positionOf = positionOf;
	}
}
