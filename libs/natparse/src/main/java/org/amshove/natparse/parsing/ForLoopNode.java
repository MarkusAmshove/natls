package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IForLoopNode;
import org.amshove.natparse.natural.IVariableReferenceNode;
import org.jspecify.annotations.Nullable;
import org.amshove.natparse.natural.IOperandNode;

class ForLoopNode extends StatementWithBodyNode implements IForLoopNode, ILabelIdentifierSettable
{
	private IVariableReferenceNode loopControl;
	private IOperandNode upperBound;
	private SyntaxToken labelIdentifier;

	@Override
	public IVariableReferenceNode loopControl()
	{
		return loopControl;
	}

	@Override
	public IOperandNode upperBound()
	{
		return upperBound;
	}

	void setUpperBound(IOperandNode operandNode)
	{
		this.upperBound = operandNode;
	}

	void setLoopControl(IVariableReferenceNode loopControl)
	{
		this.loopControl = loopControl;
	}

	@Override
	public ReadOnlyList<IOperandNode> mutations()
	{
		return ReadOnlyList.of(loopControl);
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
