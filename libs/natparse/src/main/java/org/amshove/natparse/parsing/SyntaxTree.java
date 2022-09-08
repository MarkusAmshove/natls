package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.ISyntaxNodeVisitor;
import org.amshove.natparse.natural.ISyntaxTree;

import java.util.*;

final class SyntaxTree implements ISyntaxTree
{
	private final List<ISyntaxNode> descendants;

	private SyntaxTree(List<ISyntaxNode> descendants)
	{
		this.descendants = descendants;
	}

	static ISyntaxTree create(ReadOnlyList<ISyntaxNode> descendants)
	{
		return new SyntaxTree(descendants.toList());
	}

	static ISyntaxTree create(ISyntaxNode... descendants)
	{
		return new SyntaxTree(new ArrayList<>(Arrays.asList(descendants)));
	}

	public ReadOnlyList<ISyntaxNode> descendants()
	{
		return ReadOnlyList.from(descendants); // TODO: perf
	}

	@Override
	public void accept(ISyntaxNodeVisitor visitor)
	{
		for (var descendant : descendants())
		{
			visitor.visit(descendant);
			descendant.accept(visitor);
		}
	}

	@Override
	public Iterator<ISyntaxNode> iterator()
	{
		return descendants.iterator();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;
		if (obj == null || obj.getClass() != this.getClass())
			return false;
		var that = (SyntaxTree) obj;
		return Objects.equals(this.descendants, that.descendants);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(descendants);
	}

	@Override
	public String toString()
	{
		return "SyntaxTree[" +
			"descendants=" + descendants + ']';
	}

	protected void replace(ISyntaxNode oldNode, ISyntaxNode newNode)
	{
		var oldIndex = descendants.indexOf(oldNode);
		descendants.set(oldIndex, newNode);
	}
}
