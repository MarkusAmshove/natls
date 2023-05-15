package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.ITrimFunctionNode;

import javax.annotation.Nullable;

class TrimFunctionNode extends SystemFunctionNode implements ITrimFunctionNode
{
	private SyntaxKind option;

	@Nullable
	@Override
	public SyntaxKind option()
	{
		return option;
	}

	void setOption(SyntaxKind kind)
	{
		this.option = kind;
	}
}
