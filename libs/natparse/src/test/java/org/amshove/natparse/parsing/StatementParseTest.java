package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IStatementListNode;
import org.amshove.natparse.natural.IStatementNode;
import org.amshove.testhelpers.IntegrationTest;

@IntegrationTest
public class StatementParseTest extends AbstractParserTest<IStatementListNode>
{
	protected StatementParseTest()
	{
		super(StatementListParser::new);
	}

	protected <T extends IStatementNode> T assertParsesSingleStatement(String source, Class<T> nodeType)
	{
		var result = super.assertParsesWithoutDiagnostics(source);
		return assertNodeType(result.statements().first(), nodeType);
	}

	protected <T extends IStatementNode> T assertParsesSingleStatementWithDiagnostic(String source, Class<T> nodeType, ParserError expectedDiagnostic)
	{
		var result = super.assertDiagnostic(source, expectedDiagnostic);
		return assertNodeType(result.statements().first(), nodeType);
	}
}
