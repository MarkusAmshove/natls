package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IExpandArrayNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class ExpandArrayNode extends StatementNode implements IExpandArrayNode
{
	private IVariableReferenceNode arrayToExpand;
	private IVariableReferenceNode errorVariable;

	@Override
	public IVariableReferenceNode arrayToExpand()
	{
		return arrayToExpand;
	}

	@Override
	public IVariableReferenceNode errorVariable()
	{
		return errorVariable;
	}

	void setArrayToExpand(IVariableReferenceNode array)
	{
		arrayToExpand = array;
	}

	void setErrorVariable(IVariableReferenceNode errorVariable)
	{
		this.errorVariable = errorVariable;
	}
}
