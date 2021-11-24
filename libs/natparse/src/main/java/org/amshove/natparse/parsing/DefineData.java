package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IDefineData;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.IUsingNode;
import org.amshove.natparse.natural.IVariableNode;

import java.util.ArrayList;
import java.util.List;

class DefineData extends BaseSyntaxNode implements IDefineData
{
	private List<IUsingNode> usings = new ArrayList<>();
	private List<IVariableNode> variables = new ArrayList<>();
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

	@Override
	public ReadOnlyList<IVariableNode> variables()
	{
		return ReadOnlyList.from(variables); // TODO: Perf
	}

	@Override
	public ReadOnlyList<IUsingNode> usings()
	{
		return ReadOnlyList.from(usings); // TODO: Perf
	}

	public void addUsing(UsingNode node)
	{
		usings.add(node);
	}

	public void addVariable(VariableNode node)
	{
		variables.add(node);
	}

	@Override
	protected void nodeAdded(BaseSyntaxNode node)
	{
		if(node instanceof IUsingNode)
		{
			usings.add((IUsingNode) node);
		}

		if(node instanceof IVariableNode)
		{
			variables.add((IVariableNode) node);
		}
	}
}
