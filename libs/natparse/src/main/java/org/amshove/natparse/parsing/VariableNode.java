package org.amshove.natparse.parsing;

import org.amshove.natparse.IPosition;
import org.amshove.natparse.NaturalParseException;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.IVariableNode;
import org.amshove.natparse.natural.VariableScope;

public class VariableNode extends BaseSyntaxNode implements IVariableNode
{
	private int level;
	private String name;
	private SyntaxToken declaration;
	private VariableScope scope;

	private String qualifiedName; // Gets computed on first demand

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
}
