package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.*;

import java.util.ArrayList;
import java.util.List;

class DefineDataNode extends BaseSyntaxNode implements IDefineData
{
	private final List<IUsingNode> usings = new ArrayList<>();
	private final List<IVariableNode> variables = new ArrayList<>();

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
		if (node instanceof IUsingNode usingNode)
		{
			usings.add(usingNode);
			addAllVariablesFromUsing(usingNode);
		}

		if (node instanceof IScopeNode)
		{
			addAllVariablesFromScope((IScopeNode) node);
		}
	}

	private void addAllVariablesFromUsing(IUsingNode usingNode)
	{
		if(usingNode.defineData() == null)
		{
			return;
		}

		for (var variable : usingNode.defineData().variables())
		{
			variables.add(variable);
		}
	}

	private void addAllVariablesFromScope(IScopeNode node)
	{
		for (var variable : node.variables())
		{
			addAllVariablesRecursively(variable);
		}
	}

	private void addAllVariablesRecursively(IVariableNode variable)
	{
		variables.add(variable);
		if (variable instanceof IGroupNode group)
		{
			for (var nestedVariable : group.variables())
			{
				addAllVariablesRecursively(nestedVariable);
			}
		}
	}
}
