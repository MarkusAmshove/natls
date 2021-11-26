package org.amshove.natparse.parsing;

import org.amshove.natparse.NaturalParseException;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IGroupNode;
import org.amshove.natparse.natural.IVariableNode;
import org.amshove.natparse.natural.VariableScope;

import java.util.ArrayList;
import java.util.List;

public class GroupNode extends VariableNode implements IGroupNode
{
	private List<IVariableNode> variables = new ArrayList<>();

	public GroupNode(VariableNode variable)
	{
		setLevel(variable.level());
		setDeclaration(variable.declaration());
		for (var node : variable.nodes())
		{
			addNode((BaseSyntaxNode) node);
		}
	}

	@Override
	public ReadOnlyList<IVariableNode> variables()
	{
		return ReadOnlyList.from(variables); // TODO: perf
	}

	void addVariable(VariableNode node)
	{
		if(level() >= node.level())
		{
			throw new NaturalParseException("Can not add child variable with lower level than group");
		}
		variables.add(node);
		node.setParent(this);
		addNode(node);
	}

	@Override
	void setScope(VariableScope scope)
	{
		super.setScope(scope);

		for (var variable : variables)
		{
			((VariableNode)variable).setScope(scope);
		}
	}
}
