package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

public interface IModuleReferencingNode
{
	INaturalModule reference();
	SyntaxToken referencingToken();
}
