package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IExpandArrayNode;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

import java.util.ArrayList;
import java.util.List;

class ExpandArrayNode extends StatementNode implements IExpandArrayNode
{
	private IVariableReferenceNode arrayToExpand;
	private IVariableReferenceNode errorVariable;
	private final List<IOperandNode> dimensions = new ArrayList<>();

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

	@Override
	public ReadOnlyList<IOperandNode> dimensions()
	{
		return ReadOnlyList.from(dimensions);
	}

	void setArrayToExpand(IVariableReferenceNode array)
	{
		arrayToExpand = array;
	}

	void setErrorVariable(IVariableReferenceNode errorVariable)
	{
		this.errorVariable = errorVariable;
	}

	void addDimension(IOperandNode dimension)
	{
		dimensions.add(dimension);
	}

	@Override
	public ReadOnlyList<IOperandNode> mutations()
	{
		var mutations = new ArrayList<IOperandNode>();
		mutations.add(arrayToExpand);
		if (errorVariable != null)
		{
			mutations.add(errorVariable);
		}

		return ReadOnlyList.from(mutations);
	}
}
