package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.*;

import java.util.ArrayList;
import java.util.List;

class InputStatementNode extends StatementNode implements IInputStatementNode
{
	private final List<IOutputOperandNode> operands = new ArrayList<>();
	private IAttributeListNode statementAttributes;

	@Override
	public ReadOnlyList<IOutputOperandNode> operands()
	{
		return ReadOnlyList.from(operands);
	}

	void addOperand(OutputOperandNode operand)
	{
		if (operand == null)
		{
			// stuff like tab setting, line skip etc.
			return;
		}

		operand.setParent(this);
		operands.add(operand);
	}

	void setStatementAttributes(IAttributeListNode statementAttributes)
	{
		this.statementAttributes = statementAttributes;
	}

	public ReadOnlyList<IAttributeNode> statementAttributes()
	{
		return statementAttributes == null ? ReadOnlyList.empty() : statementAttributes.attributes();
	}
}
