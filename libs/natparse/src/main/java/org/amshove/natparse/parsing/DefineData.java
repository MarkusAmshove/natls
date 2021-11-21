package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IDefineData;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.IUsingNode;

import java.util.ArrayList;
import java.util.List;

class DefineData extends BaseSyntaxNode implements IDefineData
{
	private List<IUsingNode> usings = new ArrayList<>();
	private ISyntaxNode startNode;
	private ISyntaxNode endNode;

	@Override
	public ReadOnlyList<IUsingNode> localUsings()
	{
		return usings.stream().filter(IUsingNode::isLocalUsing).collect(ReadOnlyList.collector()); // TODO: Perf
	}

	@Override
	public ReadOnlyList<IUsingNode> parameterUsings()
	{
		return usings.stream().filter(IUsingNode::isParameterUsing).collect(ReadOnlyList.collector()); // TODO: Perf
	}

	@Override
	public ReadOnlyList<IUsingNode> globalUsings()
	{
		return usings.stream().filter(IUsingNode::isGlobalUsing).collect(ReadOnlyList.collector()); // TODO: Perf
	}

	public void addUsing(UsingNode node)
	{
		addNode(node);
		usings.add(node);
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
