package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

public interface ISymbolReferenceNode extends ITokenNode
{
	IReferencableNode reference();

	SyntaxToken referencingToken();
}
