package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IDisplayNode;

import java.util.Optional;

class DisplayNode extends StatementNode implements IDisplayNode, ICanSetReportSpecification
{
	private SyntaxToken reportSpecification;

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
}
