package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.ISyntaxTree;
import org.amshove.natparse.natural.ITokenNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

record SyntaxTree(ReadOnlyList<? extends ISyntaxNode> descendants) implements ISyntaxTree
{

	static ISyntaxTree create(ReadOnlyList<ISyntaxNode> descendants)
	{
		return new SyntaxTree(descendants);
	}

	static ISyntaxTree create(ISyntaxNode... descendants)
	{
		return new SyntaxTree(ReadOnlyList.from(Arrays.asList(descendants)));
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
	public ReadOnlyList<? extends ISyntaxNode> descendants()
	{
		return descendants;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Iterator<ISyntaxNode> iterator()
	{
		return (Iterator<ISyntaxNode>) descendants.iterator();
	}
}
