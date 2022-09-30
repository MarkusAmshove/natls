package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IReportSpecificationOperandNode;

class ReportSpecificationOperandNode extends BaseSyntaxNode implements IReportSpecificationOperandNode
{
	private SyntaxToken reportSpecification;

	@Override
	public SyntaxToken reportSpecification()
	{
		return reportSpecification;
	}

	void setReportSpecification(SyntaxToken token)
	{
		reportSpecification = token;
	}
}
