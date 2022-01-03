package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxToken;

import java.util.ArrayList;

class ParserUtil
{
	static ReadOnlyList<TokenNode> createTokenNodes(ReadOnlyList<SyntaxToken> tokens)
	{
		var tokenNodes = new ArrayList<TokenNode>(tokens.size());
		for (var token : tokens)
		{
			tokenNodes.add(new TokenNode(token));
		}

		return ReadOnlyList.from(tokenNodes);
	}

	static void addTokensToNode(BaseSyntaxNode node, ReadOnlyList<SyntaxToken> tokens)
	{
		for (var token : tokens)
		{
			node.addNode(new TokenNode(token));
		}
	}
}
