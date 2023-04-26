package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IStatementListNode;
import org.junit.jupiter.api.Test;

class AbstractParserDiagnosticReportingShould extends AbstractParserTest<IStatementListNode>
{
	protected AbstractParserDiagnosticReportingShould()
	{
		super(StatementListParser::new);
	}

	@Test
	void reportAnUnexpectedTokenDiagnosticWhenEncounteringEOF()
	{
		assertDiagnostic("""
				DEFINE PRINTER (15) OUTPUT /* string literal missing
			""", ParserError.UNEXPECTED_TOKEN);
	}
}
