package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IStatementListNode;
import org.amshove.natparse.natural.IStatementNode;
import org.amshove.natparse.natural.ITokenNode;
import org.amshove.testhelpers.IntegrationTest;

import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

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
		assertHasSingleStatement(result);
		return assertNodeType(result.statements().first(), nodeType);
	}

	protected <T extends IStatementNode> T assertParsesSingleStatementWithDiagnostic(String source, Class<T> nodeType, ParserError expectedDiagnostic)
	{
		var result = super.assertDiagnostic(source, expectedDiagnostic);
		assertHasSingleStatement(result);
		return assertNodeType(result.statements().first(), nodeType);
	}

	private void assertHasSingleStatement(IStatementListNode statementList)
	{
		assertThat(statementList)
			.as("Expected single statement but got: " + statementList.statements().stream().map(this::formatStatementName).collect(Collectors.joining(", ")))
			.hasSize(1);
	}

	private String formatStatementName(IStatementNode node)
	{
		var className = node.getClass().getSimpleName();
		if (node instanceof ITokenNode tokenNode)
		{
			return "%s(%s)".formatted(className, tokenNode.token().kind());
		}

		return className;
	}
}
