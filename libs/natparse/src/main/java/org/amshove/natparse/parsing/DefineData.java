package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IDefineData;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.IUsingNode;

import java.util.ArrayList;
import java.util.List;

class DefineData extends BaseSyntaxNode implements IDefineData
{
	private List<IUsingNode> localUsings = new ArrayList<>();
	private ISyntaxNode startNode;
	private ISyntaxNode endNode;

	@Override
	public ReadOnlyList<IUsingNode> localUsings()
	{
		return ReadOnlyList.from(localUsings); // TODO: Perf
	}

	public void addLocalUsing(UsingNode node)
	{
		addNode(node);
		localUsings.add(node);
	}

	public void setStartingNode(TokenNode tokenNode)
	{
		setStart(tokenNode.token());
		addNode(tokenNode);
		startNode = tokenNode;
	}

	public void setEndNode(TokenNode tokenNode)
	{
		setEnd(tokenNode.token());
		addNode(tokenNode);
		endNode = tokenNode;
	}

}
