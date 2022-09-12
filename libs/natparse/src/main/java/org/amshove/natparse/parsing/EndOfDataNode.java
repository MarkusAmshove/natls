package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IEndOfDataNode;

import java.util.Optional;

class EndOfDataNode extends StatementWithBodyNode implements IEndOfDataNode, ICanSetReportSpecification
{
	private SyntaxToken reportSpecification;

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
}
