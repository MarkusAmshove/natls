package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxKind;

public interface IConstantAttributeNode extends IAttributeNode
{
	SyntaxKind kind();
}
