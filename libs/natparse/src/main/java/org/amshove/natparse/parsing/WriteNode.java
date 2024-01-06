package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.output.IOutputElementNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class WriteNode extends StatementNode implements IWriteNode, IPrintNode, ICanSetReportSpecification
{
	private SyntaxToken reportSpecification;
	private final List<IOutputElementNode> operands = new ArrayList<>();
	private IAttributeListNode statementAttributes;

	@Override
	public Optional<SyntaxToken> reportSpecification()
	{
		return Optional.ofNullable(reportSpecification);
	}

	@Override
	public void setReportSpecification(SyntaxToken token)
	{
		reportSpecification = token;
	}

	@Override
	public ReadOnlyList<IOutputElementNode> operands()
	{
		return ReadOnlyList.from(operands);
	}

	@Override
	public ReadOnlyList<IAttributeNode> statementAttributes()
	{
		return statementAttributes != null ? statementAttributes.attributes() : ReadOnlyList.empty();
	}

	void addOperand(IOutputElementNode operand)
	{
		if (operand == null)
		{
			// stuff like tab setting, line skip etc.
			return;
		}

		addNode((BaseSyntaxNode) operand);
		operands.add(operand);
	}

	void setAttributes(IAttributeListNode attributes)
	{
		statementAttributes = attributes;
	}
}
