package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.IVariableNode;
import org.amshove.natparse.natural.VariableScope;

public class VariableNode extends BaseSyntaxNode implements IVariableNode
{
	private int level;
	private String name;
	private SyntaxToken declaration;
	private DataFormat dataFormat;
	private double dataLength;
	private VariableScope scope;
	private boolean hasDynamicLength;

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
	public DataFormat dataFormat()
	{
		return dataFormat;
	}

	@Override
	public double dataLength()
	{
		return dataLength;
	}

	@Override
	public VariableScope scope()
	{
		return scope;
	}

	@Override
	public boolean hasDynamicLength()
	{
		return hasDynamicLength;
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

	void setDataFormat(DataFormat dataFormat)
	{
		this.dataFormat = dataFormat;
	}

	void setDataLength(double dataLength)
	{
		this.dataLength = dataLength;
	}

	void setScope(VariableScope scope)
	{
		this.scope = scope;
	}

	void setDynamicLength()
	{
		hasDynamicLength = true;
	}
}
