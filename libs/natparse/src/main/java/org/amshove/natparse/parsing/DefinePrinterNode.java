package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IDefinePrinterNode;
import org.amshove.natparse.natural.ISyntaxNode;

import java.util.Optional;

class DefinePrinterNode extends StatementNode implements IDefinePrinterNode
{
	private int number;
	private SyntaxToken name;
	private ISyntaxNode output;

	@Override
	public int printerNumber()
	{
		return number;
	}

	@Override
	public Optional<SyntaxToken> printerName()
	{
		return Optional.ofNullable(name);
	}

	@Override
	public Optional<ISyntaxNode> output()
	{
		return Optional.ofNullable(output);
	}

	void setPrinterNumber(int number)
	{
		this.number = number;
	}

	void setName(SyntaxToken token)
	{
		name = token;
	}

	void setOutput(ISyntaxNode output)
	{
		this.output = output;
	}
}
