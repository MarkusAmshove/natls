package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.ICallnatNode;
import org.amshove.natparse.natural.IStatementListNode;
import org.amshove.natparse.natural.IStatementNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class StatementParserShould extends AbstractParserTest<IStatementListNode>
{
	protected StatementParserShould()
	{
		super(new StatementListParser(null));
	}

	@Test
	void parseASimpleCallnat()
	{
		var callnat = assertParsesSingleStatement("CALLNAT 'MODULE'", ICallnatNode.class);
		assertThat(callnat.calledModule().kind()).isEqualTo(SyntaxKind.STRING);
		assertThat(callnat.calledModule().stringValue()).isEqualTo("MODULE");
	}

	private <T extends IStatementNode> T assertParsesSingleStatement(String source, Class<T> nodeType)
	{
		var result = super.assertParsesWithoutDiagnostics(source);
		return assertNodeType(result.statements().first(), nodeType);
	}
}
