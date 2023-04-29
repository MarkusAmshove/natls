package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IResizeArrayNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class ResizeArrayNode extends StatementNode implements IResizeArrayNode
{
	private IVariableReferenceNode arrayToResize;
	private IVariableReferenceNode errorVariable;

	@Override
	public IVariableReferenceNode arrayToResize()
	{
		return arrayToResize;
	}

	@Override
	public IVariableReferenceNode errorVariable()
	{
		return errorVariable;
	}

	void setArrayToResize(IVariableReferenceNode array)
	{
		arrayToResize = array;
	}

	void setErrorVariable(IVariableReferenceNode errorVariable)
	{
		this.errorVariable = errorVariable;
	}
}
