package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.*;

import java.util.ArrayList;
import java.util.List;

class DefineData extends BaseSyntaxNode implements IDefineData
{
	private List<IUsingNode> usings = new ArrayList<>();
	private List<IVariableNode> variables = new ArrayList<>();

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

	@Override
	protected void nodeAdded(BaseSyntaxNode node)
	{
		if (node instanceof IUsingNode)
		{
			usings.add((IUsingNode) node);
		}

		if (node instanceof IScopeNode)
		{
			addAllVariablesRecursively((IScopeNode) node);
		}
	}

	private void addAllVariablesRecursively(IScopeNode scopeNode)
	{
		for (var variable : scopeNode.variables())
		{
			variables.add(variable);
		}
	}
}
