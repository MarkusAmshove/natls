package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

public non-sealed interface IUsingNode extends IModuleReferencingNode, IParameterDefinitionNode
{
	/**
	 * Contains the identifier that is being "used".
	 */
	SyntaxToken target();

	SyntaxToken withBlock();

	IDefineData defineData();

	VariableScope scope();

	boolean isLocalUsing();

	boolean isGlobalUsing();

	boolean isParameterUsing();
}
