package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;

/**
 * This Exception is used to bubble parse failures up. It should be caught where error recovery can be done.
 */
class ParseError extends Throwable
{
	private final SyntaxToken errorToken;

	ParseError(SyntaxToken errorToken)
	{
		this.errorToken = errorToken;
	}

	public SyntaxToken getErrorToken()
	{
		return errorToken;
	}
}
