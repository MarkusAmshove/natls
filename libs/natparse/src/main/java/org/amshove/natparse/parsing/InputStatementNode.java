package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.output.IOutputElementNode;

import java.util.ArrayList;
import java.util.List;

class InputStatementNode extends StatementNode implements IInputStatementNode
{
	private final List<IOutputElementNode> operands = new ArrayList<>();
	private IAttributeListNode statementAttributes;

	@Override
	public ReadOnlyList<IOutputElementNode> operands()
	{
		return ReadOnlyList.from(operands);
	}

	void addOperand(IOutputElementNode operand)
	{
		if (operand == null)
		{
			// stuff like tab setting, line skip etc.
			return;
		}

		((BaseSyntaxNode) operand).setParent(this);
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
