package org.amshove.natparse.parsing;

import org.amshove.natparse.IPosition;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IReferencableNode;
import org.amshove.natparse.natural.ISymbolReferenceNode;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.ISyntaxTree;

// TODO: Only exists until all statements are parse-able
class SyntheticVariableStatementNode extends StatementNode implements ISymbolReferenceNode
{
	private final SymbolReferenceNode node;

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
	public ISyntaxTree parent()
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
}
