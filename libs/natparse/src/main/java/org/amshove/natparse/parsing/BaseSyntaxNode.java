package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.ISyntaxTree;

import java.util.ArrayList;
import java.util.List;

class BaseSyntaxNode implements ISyntaxNode
{
	private List<BaseSyntaxNode> nodes = new ArrayList<>();
	private ISyntaxTree parent;

	List<BaseSyntaxNode> getNodes()
	{
		return nodes;
	}

	public void setParent(ISyntaxTree parent)
	{
		this.parent = parent;
	}

	public ISyntaxTree parent()
	{
		return parent;
	}

	@Override
	public int offset()
	{
		return getStart().offset();
	}

	@Override
	public int offsetInLine()
	{
		return getStart().offsetInLine();
	}

	@Override
	public int line()
	{
		return getStart().line();
	}

	@Override
	public int length()
	{
		return getEnd().offset() - getStart().offset();
	}

	private ISyntaxNode getStart()
	{
		return nodes.get(0);
	}

	private ISyntaxNode getEnd()
	{
		return nodes.get(nodes.size() - 1);
	}

	void addNode(BaseSyntaxNode node)
	{
		node.setParent(this);
		nodes.add(node);
	}

	@Override
	public ReadOnlyList<? extends ISyntaxNode> nodes()
	{
		return ReadOnlyList.from(nodes); // TODO: Perf
	}
}
