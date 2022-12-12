package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IBlockNode;
import org.amshove.natparse.natural.IDefineData;
import org.amshove.natparse.natural.VariableScope;

class BlockNode extends BaseSyntaxNode implements IBlockNode
{
	private SyntaxToken parent;
	private SyntaxToken block;
	private VariableScope scope;
	private IDefineData defineData;

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

	@Override
	public IDefineData defineData()
	{
		return defineData;
	}

	@Override
	public String toString()
	{
		return "blockNode{scope=%s, parent=%s, block=%s}".formatted(scope, parent, block);
	}

	void setParent(SyntaxToken parent)
	{
		this.parent = parent;
	}

	void setBlock(SyntaxToken block)
	{
		this.block = block;
	}

	void setScope(SyntaxKind scopeKind)
	{
		this.scope = VariableScope.fromSyntaxKind(scopeKind);
	}

	void setDefineData(IDefineData defineData)
	{
		this.defineData = defineData;
	}
}
