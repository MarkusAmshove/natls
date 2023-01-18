package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IBlockNode;

class BlockNode extends BaseSyntaxNode implements IBlockNode
{
	private SyntaxToken parent;
	private SyntaxToken block;

	@Override
	public SyntaxToken parentblock()
	{
		return parent;
	}

	@Override
	public SyntaxToken block()
	{
		return block;
	}

	void setParent(SyntaxToken parent)
	{
		this.parent = parent;
	}

	void setBlock(SyntaxToken block)
	{
		this.block = block;
	}

}
