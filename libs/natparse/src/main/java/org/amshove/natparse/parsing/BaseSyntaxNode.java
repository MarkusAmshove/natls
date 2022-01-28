package org.amshove.natparse.parsing;

import org.amshove.natparse.IPosition;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.ISyntaxTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class BaseSyntaxNode implements ISyntaxNode
{
	private List<BaseSyntaxNode> nodes = Collections.emptyList();
	private ISyntaxTree parent;

	public void setParent(ISyntaxTree parent)
	{
		this.parent = parent;
	}

	public ISyntaxTree parent()
	{
		return parent;
	}

	private ISyntaxNode getStart()
	{
		return nodes.get(0);
	}

	void addNode(BaseSyntaxNode node)
	{
		if(node == null)
		{
			return;
		}

		if(nodes.isEmpty())
		{
			nodes = new ArrayList<>(); // perf: We reuse an empty list because we expect a lot of nodes to not have descendants.
		}

		node.setParent(this);
		nodes.add(node);
		nodeAdded(node);
	}

	protected void nodeAdded(BaseSyntaxNode node)
	{

	}

	@Override
	public ReadOnlyList<? extends ISyntaxNode> descendants()
	{
		return ReadOnlyList.from(nodes); // TODO: Perf
	}

	@Override
	public IPosition position()
	{
		return getStart().position();
	}

	@Override
	public void destroy()
	{
		for (var descendant : nodes)
		{
			descendant.destroy();
		}
	}
}
