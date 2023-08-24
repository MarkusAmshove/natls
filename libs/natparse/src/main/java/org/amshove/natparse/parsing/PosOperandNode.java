package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IPosOperandNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class PosOperandNode extends BaseSyntaxNode implements IPosOperandNode
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
