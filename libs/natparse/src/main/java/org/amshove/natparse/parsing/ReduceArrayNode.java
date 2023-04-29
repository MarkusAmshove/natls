package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IReduceArrayNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class ReduceArrayNode extends StatementNode implements IReduceArrayNode
{
	private IVariableReferenceNode arrayToReduce;
	private IVariableReferenceNode errorVariable;

	@Override
	public IVariableReferenceNode arrayToReduce()
	{
		return arrayToReduce;
	}

	@Override
	public IVariableReferenceNode errorVariable()
	{
		return errorVariable;
	}

	void setArrayToReduce(IVariableReferenceNode array)
	{
		arrayToReduce = array;
	}

	void setErrorVariable(IVariableReferenceNode errorVariable)
	{
		this.errorVariable = errorVariable;
	}
}
