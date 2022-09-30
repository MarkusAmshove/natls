package org.amshove.natparse.parsing;

import java.util.Optional;

import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.IReportSpecificationOperandNode;
import org.amshove.natparse.natural.ISkipStatementNode;

class SkipStatementNode extends StatementNode implements ISkipStatementNode
{
	private IOperandNode toSkip;
	private IReportSpecificationOperandNode reportSpecification;

	@Override
	public IOperandNode toSkip()
	{
		return toSkip;
	}

	@Override
	public Optional<IReportSpecificationOperandNode> reportSpecification()
	{
		return Optional.ofNullable(reportSpecification);
	}

	void setToSkip(IOperandNode toSkip)
	{
		this.toSkip = toSkip;
	}

	void setReportSpecification(IReportSpecificationOperandNode node)
	{
		reportSpecification = node;
	}
}
