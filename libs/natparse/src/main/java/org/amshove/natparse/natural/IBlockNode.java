package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

public interface IBlockNode extends ISyntaxNode
{
	SyntaxToken parentblock();

	SyntaxToken block();

}
