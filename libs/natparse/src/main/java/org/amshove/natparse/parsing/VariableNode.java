package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IVariableNode;
import org.amshove.natparse.natural.IVariableType;
import org.amshove.natparse.natural.VariableScope;

public class VariableNode extends BaseSyntaxNode implements IVariableNode
{
	private int level;
	private String name;
	private SyntaxToken declaration;
	private VariableScope scope;
	private VariableType type;

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
		return level == 1 ? name : null; // TODO: walk parents
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
	public IVariableType type()
	{
		return type;
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

	void setType(VariableType type)
	{
		this.type = type;
	}
}
