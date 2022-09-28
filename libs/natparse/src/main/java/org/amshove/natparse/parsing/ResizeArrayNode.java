package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IResizeArrayNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class ResizeArrayNode extends StatementNode implements IResizeArrayNode
{
	private IVariableReferenceNode arrayToResize;

	@Override
	public IVariableReferenceNode arrayToResize()
	{
		return arrayToResize;
	}

	void setArrayToResize(IVariableReferenceNode array)
	{
		arrayToResize = array;
	}
}
