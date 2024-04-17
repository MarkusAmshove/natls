package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.INewPageNode;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.output.IOutputElementNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class NewPageNode extends StatementNode implements INewPageNode, ICanSetReportSpecification
{
	private SyntaxToken reportSpecification;
	private final List<IOutputElementNode> operands = new ArrayList<>();

	@Override
	public Optional<SyntaxToken> reportSpecification()
	{
		return Optional.ofNullable(reportSpecification);
	}

	@Override
	public void setReportSpecification(SyntaxToken reportSpecification)
	{
		this.reportSpecification = reportSpecification;
	}

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

		addNode((BaseSyntaxNode) operand);
		operands.add(operand);
	}

}
