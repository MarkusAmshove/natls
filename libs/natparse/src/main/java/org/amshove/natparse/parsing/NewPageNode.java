package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.INewPageNode;

import java.util.Optional;

class NewPageNode extends StatementNode implements INewPageNode
{
	private SyntaxToken reportSpecification;

	@Override
	public Optional<SyntaxToken> reportSpecification()
	{
		return Optional.ofNullable(reportSpecification);
	}

	void setReportSpecification(SyntaxToken reportSpecification)
	{
		this.reportSpecification = reportSpecification;
	}
}
