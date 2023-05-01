package org.amshove.natparse.parsing;

import java.util.ArrayList;
import java.util.List;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.ISystemFunctionNode;

class SystemFunctionNode extends BaseSyntaxNode implements ISystemFunctionNode
{
	private final List<IOperandNode> parameter = new ArrayList<>();
	private SyntaxKind systemFunction;

	@Override
	public SyntaxKind systemFunction()
	{
		return systemFunction;
	}

	@Override
	public ReadOnlyList<IOperandNode> parameter()
	{
		return ReadOnlyList.from(parameter);
	}

	void addParameter(IOperandNode parameter)
	{
		this.parameter.add(parameter);
	}

	void setSystemFunction(SyntaxKind systemFunction)
	{
		this.systemFunction = systemFunction;
	}
}
