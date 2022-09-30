package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IResizeDynamicNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class ResizeDynamicNode extends StatementNode implements IResizeDynamicNode
{
	private IVariableReferenceNode variableToResize;
	private int sizeToResizeTo;

	@Override
	public IVariableReferenceNode variableToResize()
	{
		return variableToResize;
	}

	@Override
	public int sizeToResizeTo()
	{
		return sizeToResizeTo;
	}


	void setVariableToResize(IVariableReferenceNode variableToResize)
	{
		this.variableToResize = variableToResize;
	}

	void setSizeToResizeTo(int sizeToResizeTo)
	{
		this.sizeToResizeTo = sizeToResizeTo;
	}
}
