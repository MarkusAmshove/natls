package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxKind;

public interface IAttributeNode extends ITokenNode
{
	SyntaxKind kind();

	String value();
}
