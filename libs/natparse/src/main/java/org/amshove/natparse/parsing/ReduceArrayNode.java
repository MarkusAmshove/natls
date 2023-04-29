package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IReduceArrayNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class ReduceArrayNode extends StatementNode implements IReduceArrayNode
{
	private IVariableReferenceNode arrayToReduce;

	@Override
	public IVariableReferenceNode arrayToReduce()
	{
		return arrayToReduce;
	}

	void setArrayToReduce(IVariableReferenceNode array)
	{
		arrayToReduce = array;
	}
}
