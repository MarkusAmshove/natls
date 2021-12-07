package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

public interface ISymbolNode extends ISyntaxNode, IReferencableNode
{
	SyntaxToken declaration(); // TODO: TokenNode?
}
