package org.amshove.natparse.parsing;

import org.amshove.natparse.IPosition;
import org.amshove.natparse.NaturalParseException;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.*;

import java.util.ArrayList;
import java.util.List;

class VariableNode extends BaseSyntaxNode implements IVariableNode
{
	private int level;
	private String name;
	private SyntaxToken declaration;
	private VariableScope scope;
	private List<IArrayDimension> dimensions = new ArrayList<>();
	private List<ISymbolReferenceNode> references = new ArrayList<>();

	private String qualifiedName; // Gets computed on first demand

	@Override
	public ReadOnlyList<ISymbolReferenceNode> references()
	{
		return ReadOnlyList.from(references); // TODO: Perf
	}

	@Override
	public SyntaxToken declaration()
	{
		return declaration;
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public String qualifiedName()
	{
		if (qualifiedName != null)
		{
			return qualifiedName;
		}

		if (level == 1)
		{
			qualifiedName = name;
			return name;
		}

		var parent = parent();
		while (parent != null)
		{
			if (parent instanceof IVariableNode && ((IVariableNode) parent).level() == 1)
			{
				qualifiedName = "%s.%s".formatted(((IVariableNode) parent).name(), name());
				return qualifiedName;
			}

			parent = ((ISyntaxNode)parent).parent();
		}

		throw new NaturalParseException("Could not determine qualified name");
	}

	@Override
	public ReadOnlyList<IArrayDimension> dimensions()
	{
		return ReadOnlyList.from(dimensions); // TODO: perf
	}

	@Override
	public int level()
	{
		return level;
	}

	@Override
	public VariableScope scope()
	{
		return scope;
	}

	@Override
	public IPosition position()
	{
		return declaration;
	}

	void setLevel(int level)
	{
		this.level = level;
	}

	void setDeclaration(SyntaxToken token)
	{
		name = token.source();
		declaration = token;
	}

	void setScope(VariableScope scope)
	{
		this.scope = scope;
	}

	void addDimension(ArrayDimension dimension)
	{
		dimensions.add(dimension);
		addNode(dimension);
	}

	void addReference(SymbolReferenceNode tokenNode)
	{
		references.add(tokenNode);
		tokenNode.setReference(this);
	}
}
