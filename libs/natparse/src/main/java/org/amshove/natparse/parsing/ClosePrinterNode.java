package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IClosePrinterNode;

class ClosePrinterNode extends StatementNode implements IClosePrinterNode
{
	private SyntaxToken printerToken;

	@Override
	public SyntaxToken printer()
	{
		return printerToken;
	}

	void setPrinter(SyntaxToken token)
	{
		printerToken = token;
	}
}
