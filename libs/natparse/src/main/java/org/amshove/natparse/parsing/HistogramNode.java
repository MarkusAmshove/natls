package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IHistogramNode;
import org.amshove.natparse.natural.IVariableReferenceNode;
import org.amshove.natparse.natural.conditionals.IConditionNode;
import org.jspecify.annotations.Nullable;

class HistogramNode extends StatementWithBodyNode implements IHistogramNode, ILabelIdentifierSettable
{
	private IVariableReferenceNode view;
	private SyntaxToken descriptor;
	private IConditionNode condition;
	private SyntaxToken labelIdentifier;

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

	@Override
	public @Nullable SyntaxToken labelIdentifier()
	{
		return labelIdentifier;
	}

	@Override
	public void setLabelIdentifier(SyntaxToken labelIdentifier)
	{
		this.labelIdentifier = labelIdentifier;
	}

}
