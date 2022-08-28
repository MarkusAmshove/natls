package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IWriteNode;

import java.util.Optional;

class WriteNode extends StatementNode implements IWriteNode
{
	private SyntaxToken reportSpecification;

	@Override
	public Optional<SyntaxToken> reportSpecification()
	{
		return Optional.ofNullable(reportSpecification);
	}

	void setReportSpecification(SyntaxToken token)
	{
		reportSpecification = token;
	}
}
