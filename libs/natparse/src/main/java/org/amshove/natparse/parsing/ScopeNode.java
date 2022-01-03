package org.amshove.natparse.parsing;

import org.amshove.natparse.NaturalParseException;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IScopeNode;
import org.amshove.natparse.natural.IVariableNode;
import org.amshove.natparse.natural.VariableScope;

import java.util.ArrayList;
import java.util.List;

class ScopeNode extends BaseSyntaxNode implements IScopeNode
{

	private VariableScope scope;
	private final List<IVariableNode> variables = new ArrayList<>();

	@Override
	public VariableScope scope()
	{
		return scope;
	}

	@Override
	public ReadOnlyList<IVariableNode> variables()
	{
		return ReadOnlyList.from(variables); // TODO: perf
	}

	void setScope(VariableScope scope)
	{
		this.scope = scope;
	}

	void addVariable(VariableNode variable)
	{
		if (variable.level() != 1)
		{
			throw new NaturalParseException("Can't add variable with a level higher than 1");
		}

		variable.setParent(this);
		addNode(variable);
		variables.add(variable);
	}

	@Override
	public String toString()
	{
		return "ScopeNode{scope=" + scope + '}';
	}
}
