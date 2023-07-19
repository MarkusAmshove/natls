package org.amshove.natparse.parsing;

import java.util.ArrayList;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.IReduceDynamicNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class ReduceDynamicNode extends StatementNode implements IReduceDynamicNode
{
	private IVariableReferenceNode variableToReduce;
	private IVariableReferenceNode errorVariable;
	private IOperandNode sizeToReduceTo;

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
	public IOperandNode sizeToReduceTo()
	{
		return sizeToReduceTo;
	}

	void setVariableToResize(IVariableReferenceNode variableToReduce)
	{
		this.variableToReduce = variableToReduce;
	}

	void setSizeToResizeTo(IOperandNode sizeToReduceTo)
	{
		this.sizeToReduceTo = sizeToReduceTo;
	}

	void setErrorVariable(IVariableReferenceNode errorVariable)
	{
		this.errorVariable = errorVariable;
	}

	@Override
	public ReadOnlyList<IOperandNode> mutations()
	{
		var mutations = new ArrayList<IOperandNode>();
		mutations.add(variableToReduce);
		if (errorVariable != null)
		{
			mutations.add(errorVariable);
		}

		return ReadOnlyList.from(mutations);
	}
}
