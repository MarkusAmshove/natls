package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.ISeparateStatementNode;

class SeparateStatementNode extends StatementNode implements ISeparateStatementNode
{
	private IOperandNode separated;

	@Override
	public IOperandNode separated()
	{
		return separated;
	}

	void setSeparated(IOperandNode separated)
	{
		this.separated = separated;
	}
}
