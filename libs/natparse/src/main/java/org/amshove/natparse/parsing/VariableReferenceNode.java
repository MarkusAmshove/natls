package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

import java.util.ArrayList;
import java.util.List;

class VariableReferenceNode extends SymbolReferenceNode implements IVariableReferenceNode
{
	private final List<IOperandNode> dimensions = new ArrayList<>();

	public VariableReferenceNode(SyntaxToken token)
	{
		super(token);
	}

	void addDimension(IOperandNode operandNode)
	{
		dimensions.add(operandNode);
	}

	@Override
	public ReadOnlyList<IOperandNode> dimensions()
	{
		return ReadOnlyList.from(dimensions);
	}
}
