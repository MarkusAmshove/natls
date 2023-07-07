package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.ddm.IDdmField;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	public ReadOnlyList<IParameterDefinitionNode> parameterInOrder()
	{
		var allParameter = Stream.of(
			parameterUsings().stream(),
			variables.stream().filter(v -> v.position().isSameFileAs(position()) && v.scope().isParameter() && !(v instanceof IRedefinitionNode))
		)
			.flatMap(s -> s)
			.sorted(Comparator.comparingInt(n -> n.diagnosticPosition().line()));

		return ReadOnlyList.from(
			allParameter.collect(Collectors.toList())
		);
	}

	@Override
	public ReadOnlyList<IVariableNode> variables()
	{
		return ReadOnlyList.from(variables); // TODO: Perf
	}

	@Nullable
	@Override
	public IVariableNode findVariable(String symbolName)
	{
		for (var variable : variables)
		{
			if (variable.name().equals(symbolName) || variable.qualifiedName().equals(symbolName))
			{
				return variable;
			}
		}

		return null;
	}

	@Nullable
	@Override
	public IDdmField findDdmField(String symbolName)
	{
		for (var variable : variables)
		{
			if (variable instanceof IViewNode viewNode)
			{
				if (viewNode.ddm() == null)
				{
					return null;
				}

				var field = viewNode.ddm().findField(symbolName);
				if (field != null)
				{
					return field;
				}
			}
		}

		return null;
	}

	@Nullable
	@Override
	public IScopeNode findFirstScopeNode(VariableScope scope)
	{
		for (var descendant : descendants())
		{
			if (descendant instanceof IScopeNode scopeNode && scopeNode.scope() == scope)
			{
				return scopeNode;
			}
		}

		return null;
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
		if (usingNode.defineData() == null)
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

	List<IVariableNode> findVariablesWithName(String symbolName)
	{
		var foundVariables = new ArrayList<IVariableNode>();
		for (var variable : variables)
		{
			if (variable.name() == null)
			{
				continue; // There was a parse error with that variable name
			}

			if ((variable.name().equals(symbolName) || variable.qualifiedName().equals(symbolName))
				&& !(variable instanceof IRedefinitionNode))
			{
				foundVariables.add(variable);
			}
		}

		return foundVariables;
	}
}
