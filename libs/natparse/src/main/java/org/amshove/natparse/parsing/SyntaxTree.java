package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.ISyntaxTree;
import org.amshove.natparse.natural.ITokenNode;

import java.util.ArrayList;

record SyntaxTree(ReadOnlyList<? extends ISyntaxNode> children) implements ISyntaxTree
{

	static ISyntaxTree create(ReadOnlyList<ISyntaxNode> children)
	{
		return new SyntaxTree(children);
	}

	static ISyntaxTree createFromTokens(ReadOnlyList<SyntaxToken> tokens)
	{
		var tokenNodes = new ArrayList<ITokenNode>(tokens.size());
		for (var token : tokens)
		{
			tokenNodes.add(new TokenNode(token));
		}

		return new SyntaxTree(ReadOnlyList.from(tokenNodes));
	}

	@Override
	public ReadOnlyList<? extends ISyntaxNode> nodes()
	{
		return children;
	}
}
