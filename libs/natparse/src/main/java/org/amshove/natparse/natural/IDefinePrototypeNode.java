package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

import javax.annotation.Nullable;

public interface IDefinePrototypeNode extends IStatementNode
{
	/**
	 * Contains the name of the prototype. Could be a reference to an external function or a variable reference if
	 * {@code isVariable()} is true.
	 */
	SyntaxToken nameToken();

	boolean isVariable();

	/**
	 * The reference to the variable containing the function name. Is filled if {@code isVariable()} is true.
	 */
	@Nullable
	IVariableReferenceNode variableReference();
}
