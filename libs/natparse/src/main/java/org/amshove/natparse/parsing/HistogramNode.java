package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IHistogramNode;
import org.amshove.natparse.natural.IVariableReferenceNode;
import org.amshove.natparse.natural.conditionals.IConditionNode;

class HistogramNode extends StatementWithBodyNode implements IHistogramNode
{
	private IVariableReferenceNode view;
	private SyntaxToken descriptor;
	private IConditionNode condition;

	@Override
	public IVariableReferenceNode view()
	{
		return view;
	}

	@Override
	public SyntaxToken descriptor()
	{
		return descriptor;
	}

	@Override
	public IConditionNode condition()
	{
		return condition;
	}

	void setView(IVariableReferenceNode view)
	{
		this.view = view;
	}

	void setDescriptor(SyntaxToken descriptor)
	{
		this.descriptor = descriptor;
	}

	void setCondition(ConditionNode condition)
	{
		addNode(condition);
		this.condition = condition;
	}

}
