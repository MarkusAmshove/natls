package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.IReduceArrayNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

import java.util.ArrayList;
import java.util.List;

class ReduceArrayNode extends StatementNode implements IReduceArrayNode
{
	private IVariableReferenceNode arrayToReduce;
	private IVariableReferenceNode errorVariable;
	private final List<IOperandNode> dimensions = new ArrayList<>();

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

	@Override
	public ReadOnlyList<IOperandNode> dimensions()
	{
		return ReadOnlyList.from(dimensions);
	}

	void setArrayToReduce(IVariableReferenceNode array)
	{
		arrayToReduce = array;
	}

	void setErrorVariable(IVariableReferenceNode errorVariable)
	{
		this.errorVariable = errorVariable;
	}

	void addDimension(IOperandNode operand)
	{
		dimensions.add(operand);
	}

	@Override
    public ReadOnlyList<IOperandNode> mutations()
    {
        var mutations = new ArrayList<IOperandNode>();
        mutations.add(arrayToReduce);
		if (errorVariable != null)
		{
			mutations.add(errorVariable);
		}

        return ReadOnlyList.from(mutations);
    }
}
