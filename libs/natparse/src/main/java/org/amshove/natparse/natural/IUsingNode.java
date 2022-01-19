package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

public interface IUsingNode extends ISyntaxNode, IModuleReferencingNode
{
	/**
	 * Contains the identifier that is being "used".
	 */
	SyntaxToken target();

	IDefineData defineData();

	boolean isLocalUsing();
	boolean isGlobalUsing();
	boolean isParameterUsing();
}
