package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IBreakOfNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

import java.util.Optional;

class BreakOfNode extends StatementWithBodyNode implements IBreakOfNode, ICanSetReportSpecification
{
	private SyntaxToken reportSpecification;
	private IVariableReferenceNode operand;

	@Override
	public void setReportSpecification(SyntaxToken reportSpecification)
	{
		this.reportSpecification = reportSpecification;
	}

	@Override
	public Optional<SyntaxToken> reportSpecification()
	{
		return Optional.ofNullable(reportSpecification);
	}

	@Override
	public IVariableReferenceNode operand()
	{
		return operand;
	}

	void setOperand(IVariableReferenceNode reference)
	{
		operand = reference;
	}
}
