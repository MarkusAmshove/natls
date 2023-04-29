package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IExpandDynamicNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class ExpandDynamicNode extends StatementNode implements IExpandDynamicNode
{
	private IVariableReferenceNode variableToExpand;
	private int sizeToExpandTo;

	@Override
	public IVariableReferenceNode variableToExpand()
	{
		return variableToExpand;
	}

	@Override
	public int sizeToExpandTo()
	{
		return sizeToExpandTo;
	}

	void setVariableToResize(IVariableReferenceNode variableToExpand)
	{
		this.variableToExpand = variableToExpand;
	}

	void setSizeToResizeTo(int sizeToExpandTo)
	{
		this.sizeToExpandTo = sizeToExpandTo;
	}
}
