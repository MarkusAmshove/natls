package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

public interface ITokenNode extends ISyntaxNode
{
	SyntaxToken token();
}
