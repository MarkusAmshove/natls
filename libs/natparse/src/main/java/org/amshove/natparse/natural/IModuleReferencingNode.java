package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

public interface IModuleReferencingNode extends ISyntaxNode
{
	/**
	 * The referenced module.
	 */
	INaturalModule reference();

	/**
	 * Contains the name {@link SyntaxToken} which holds the name of the called module.
	 */
	SyntaxToken referencingToken();
}
