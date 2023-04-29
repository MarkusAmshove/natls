package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IReduceDynamicNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class ReduceDynamicNode extends StatementNode implements IReduceDynamicNode
{
	private IVariableReferenceNode variableToReduce;
	private IVariableReferenceNode errorVariable;
	private int sizeToReduceTo;

	@Override
	public IVariableReferenceNode variableToReduce()
	{
		return variableToReduce;
	}

	@Override
	public IVariableReferenceNode errorVariable()
	{
		return errorVariable;
	}

	@Override
	public int sizeToReduceTo()
	{
		return sizeToReduceTo;
	}

	void setVariableToResize(IVariableReferenceNode variableToReduce)
	{
		this.variableToReduce = variableToReduce;
	}

	void setSizeToResizeTo(int sizeToReduceTo)
	{
		this.sizeToReduceTo = sizeToReduceTo;
	}

	void setErrorVariable(IVariableReferenceNode errorVariable)
	{
		this.errorVariable = errorVariable;
	}
}
