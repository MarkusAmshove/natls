package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IDivideStatementNode;
import org.amshove.natparse.natural.IOperandNode;

class DivideStatementNode extends BasicMathStatementNode implements IDivideStatementNode
{
	private IOperandNode giving;
	private IOperandNode remainder;

	@Override
	public IOperandNode giving()
	{
		return giving;
	}

	@Override
	public IOperandNode remainder()
	{
		return remainder;
	}

	@Override
	public boolean hasRemainder()
	{
		return remainder != null;
	}

	void setGiving(IOperandNode giving)
	{
		this.giving = giving;
	}

	void setRemainder(IOperandNode remainder)
	{
		this.remainder = remainder;
	}

	@Override
	public ReadOnlyList<IOperandNode> mutations()
	{
		return ReadOnlyList.of(target(), giving, remainder);
	}
}
