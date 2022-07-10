package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.ISystemFunctionNode;

class SystemFunctionNode extends BaseSyntaxNode implements ISystemFunctionNode
{
	private IOperandNode parameter;
	private SyntaxKind systemFunction;

	@Override
	public SyntaxKind systemFunction()
	{
		return systemFunction;
	}

	@Override
	public IOperandNode parameter()
	{
		return parameter;
	}

	void setParameter(IOperandNode parameter)
	{
		this.parameter = parameter;
	}

	void setSystemFunction(SyntaxKind systemFunction)
	{
		this.systemFunction = systemFunction;
	}
}
