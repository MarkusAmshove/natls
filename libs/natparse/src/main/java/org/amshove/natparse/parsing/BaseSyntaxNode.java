package org.amshove.natparse.parsing;

import org.amshove.natparse.IPosition;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.ISyntaxTree;

import java.util.ArrayList;
import java.util.List;

class BaseSyntaxNode implements ISyntaxNode
{

	private IPosition start;
	private IPosition end;
	private List<ISyntaxNode> nodes = new ArrayList<>();
	private ISyntaxTree parent;

	public void setParent(ISyntaxTree parent)
	{
		this.parent = parent;
	}

	public ISyntaxTree parent()
	{
		return parent;
	}

	public void setStart(IPosition start)
	{
		this.start = start;
	}

	public void setEnd(IPosition end)
	{
		this.end = end;
	}

	@Override
	public int offset()
	{
		return start.offset();
	}

	@Override
	public int offsetInLine()
	{
		return start.offsetInLine();
	}

	@Override
	public int line()
	{
		return start.line();
	}

	@Override
	public int length()
	{
		return end.offset() - start.offset();
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
