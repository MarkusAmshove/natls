package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.ITopOfPageNode;

import java.util.Optional;

class TopOfPageNode extends StatementWithBodyNode implements ITopOfPageNode
{
	private SyntaxToken reportSpecification;

	void setReportSpecification(SyntaxToken reportSpecification)
	{
		this.reportSpecification = reportSpecification;
	}

	@Override
	public Optional<SyntaxToken> reportSpecification()
	{
		return Optional.ofNullable(reportSpecification);
	}
}
