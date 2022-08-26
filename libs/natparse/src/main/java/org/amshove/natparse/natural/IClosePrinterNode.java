package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

public interface IClosePrinterNode extends IStatementNode
{
	/**
	 * The printer to close. Either IDENTIFIER or NUMBER_LITERAL.
	 */
	SyntaxToken printer();
}
