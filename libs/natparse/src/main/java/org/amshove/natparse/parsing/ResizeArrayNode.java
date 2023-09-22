package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.IResizeArrayNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

import java.util.ArrayList;
import java.util.List;

class ResizeArrayNode extends StatementNode implements IResizeArrayNode
{
	private IVariableReferenceNode arrayToResize;
	private IVariableReferenceNode errorVariable;
	private final List<IOperandNode> dimensions = new ArrayList<>();

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

	@Override
	public ReadOnlyList<IOperandNode> dimensions()
	{
		return ReadOnlyList.from(dimensions);
	}

	void setArrayToResize(IVariableReferenceNode array)
	{
		arrayToResize = array;
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
		mutations.add(arrayToResize);
		if (errorVariable != null)
		{
			mutations.add(errorVariable);
		}

		return ReadOnlyList.from(mutations);
	}
}
