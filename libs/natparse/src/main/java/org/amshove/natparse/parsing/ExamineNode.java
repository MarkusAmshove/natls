package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IExamineNode;
import org.amshove.natparse.natural.IOperandNode;

class ExamineNode extends StatementNode implements IExamineNode
{
	private IOperandNode examined;

	@Override
	public IOperandNode examined()
	{
		return examined;
	}

	void setExamined(IOperandNode examined)
	{
		this.examined = examined;
	}
}
