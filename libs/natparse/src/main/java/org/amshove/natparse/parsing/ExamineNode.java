package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IExamineNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class ExamineNode extends StatementNode implements IExamineNode
{
	private IVariableReferenceNode examined;

	@Override
	public IVariableReferenceNode examinedVariable()
	{
		return examined;
	}

	void setExamined(IVariableReferenceNode examined)
	{
		this.examined = examined;
	}
}
