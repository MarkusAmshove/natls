package org.amshove.natparse.parsing;

import org.amshove.natparse.IPosition;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.*;

import java.util.ArrayList;
import java.util.List;

// TODO: Only exists until all statements are parse-able
class SyntheticVariableStatementNode extends StatementNode implements IVariableReferenceNode
{
	private final SymbolReferenceNode node;
	private final List<IOperandNode> dimensions = new ArrayList<>();

	public SyntheticVariableStatementNode(SymbolReferenceNode node)
	{
		this.node = node;
	}

	@Override
	public IReferencableNode reference()
	{
		return node.reference();
	}

	@Override
	public SyntaxToken referencingToken()
	{
		return node.token();
	}

	@Override
	public ISyntaxNode parent()
	{
		return node.parent();
	}

	@Override
	public IPosition position()
	{
		return node.position();
	}

	@Override
	public void destroy()
	{
		node.destroy();
	}

	@Override
	public ReadOnlyList<? extends ISyntaxNode> descendants()
	{
		return node.descendants();
	}

	@Override
	public SyntaxToken token()
	{
		return node.token();
	}

	@Override
	public IPosition diagnosticPosition()
	{
		return node.diagnosticPosition();
	}

	@Override
	public ReadOnlyList<IOperandNode> dimensions()
	{
		return ReadOnlyList.from(dimensions);
	}

	void addDimension(IOperandNode dimension)
	{
		dimensions.add(dimension);
	}
}
