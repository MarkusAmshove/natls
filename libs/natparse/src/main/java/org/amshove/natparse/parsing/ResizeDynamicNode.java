package org.amshove.natparse.parsing;

import java.util.ArrayList;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.IResizeDynamicNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class ResizeDynamicNode extends StatementNode implements IResizeDynamicNode
{
	private IVariableReferenceNode variableToResize;
	private IVariableReferenceNode errorVariable;

	private IOperandNode sizeToResizeTo;

	@Override
	public IVariableReferenceNode variableToResize()
	{
		return variableToResize;
	}

	@Override
	public IOperandNode sizeToResizeTo()
	{
		return sizeToResizeTo;
	}

	@Override
	public IVariableReferenceNode errorVariable()
	{
		return errorVariable;
	}

	void setVariableToResize(IVariableReferenceNode variableToResize)
	{
		this.variableToResize = variableToResize;
	}

	void setSizeToResizeTo(IOperandNode sizeToResizeTo)
	{
		this.sizeToResizeTo = sizeToResizeTo;
	}

	void setErrorVariable(IVariableReferenceNode errorVariable)
	{
		this.errorVariable = errorVariable;
	}

	@Override
	public ReadOnlyList<IOperandNode> mutations()
	{
		var mutations = new ArrayList<IOperandNode>();
		mutations.add(variableToResize);
		if (errorVariable != null)
		{
			mutations.add(errorVariable);
		}

		return ReadOnlyList.from(mutations);
	}
}
