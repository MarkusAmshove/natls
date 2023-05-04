package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IExpandDynamicNode;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class ExpandDynamicNode extends StatementNode implements IExpandDynamicNode
{
	private IVariableReferenceNode variableToExpand;
	private IVariableReferenceNode errorVariable;
	private IOperandNode sizeToExpandTo;

	@Override
	public IVariableReferenceNode variableToExpand()
	{
		return variableToExpand;
	}

	@Override
	public IOperandNode sizeToExpandTo()
	{
		return sizeToExpandTo;
	}

	@Override
	public IVariableReferenceNode errorVariable()
	{
		return errorVariable;
	}

	void setVariableToResize(IVariableReferenceNode variableToExpand)
	{
		this.variableToExpand = variableToExpand;
	}

	void setSizeToResizeTo(IOperandNode sizeToExpandTo)
	{
		this.sizeToExpandTo = sizeToExpandTo;
	}

	void setErrorVariable(IVariableReferenceNode errorVariable)
	{
		this.errorVariable = errorVariable;
	}
}
