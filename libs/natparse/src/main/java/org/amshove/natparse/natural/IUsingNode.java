package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

public interface IUsingNode extends ISyntaxNode
{
	/**
	 * Contains the identifier that is being "used".
	 */
	SyntaxToken target();

	boolean isLocalUsing();
	boolean isGlobalUsing();
	boolean isParameterUsing();
}
