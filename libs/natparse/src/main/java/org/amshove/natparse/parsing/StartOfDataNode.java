package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IStartOfDataNode;

import java.util.Optional;

class StartOfDataNode extends StatementWithBodyNode implements IStartOfDataNode, ICanSetReportSpecification
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
