package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

import java.util.Optional;

public interface IDefinePrinterNode extends IStatementNode
{
	int printerNumber();

	Optional<SyntaxToken> printerName();

	/**
	 * Points to the {@link ISyntaxNode} the output refers to.<br/>
	 * It may be either {@link ISymbolReferenceNode} for variables or {@link ILiteralNode} for alphanumerics.
	 */
	Optional<ISyntaxNode> output();
}
