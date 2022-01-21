package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.ICallnatNode;
import org.amshove.natparse.natural.IIncludeNode;
import org.amshove.natparse.natural.IStatementListNode;
import org.amshove.natparse.natural.IStatementNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class StatementListParserShould extends AbstractParserTest<IStatementListNode>
{
	protected StatementListParserShould()
	{
		super(StatementListParser::new);
	}

	@Test
	void parseASimpleCallnat()
	{
		ignoreModuleProvider();
		var callnat = assertParsesSingleStatement("CALLNAT 'MODULE'", ICallnatNode.class);
		assertThat(callnat.referencingToken().kind()).isEqualTo(SyntaxKind.STRING);
		assertThat(callnat.referencingToken().stringValue()).isEqualTo("MODULE");
	}

	@Test
	void addBidirectionalReferencesForCallnats()
	{
		var calledSubprogram = new NaturalModule(null);
		moduleProvider.addModule("A-MODULE", calledSubprogram);

		var callnat = assertParsesSingleStatement("CALLNAT 'A-MODULE'", ICallnatNode.class);
		assertThat(callnat.reference()).isEqualTo(calledSubprogram);
		assertThat(calledSubprogram.callers()).contains(callnat);
	}

	@Test
	void parseASimpleInclude()
	{
		ignoreModuleProvider();
		var include = assertParsesSingleStatement("INCLUDE L4NLOGIT", IIncludeNode.class);
		assertThat(include.referencingToken().kind()).isEqualTo(SyntaxKind.IDENTIFIER_OR_KEYWORD);
		assertThat(include.referencingToken().symbolName()).isEqualTo("L4NLOGIT");
	}

	@Test
	void addABidirectionalReferenceForIncludes()
	{
		var includedCopycode = new NaturalModule(null);
		moduleProvider.addModule("L4NLOGIT", includedCopycode);

		var include = assertParsesSingleStatement("INCLUDE L4NLOGIT", IIncludeNode.class);
		assertThat(include.reference()).isEqualTo(includedCopycode);
		assertThat(includedCopycode.callers()).contains(include);
	}

	private <T extends IStatementNode> T assertParsesSingleStatement(String source, Class<T> nodeType)
	{
		var result = super.assertParsesWithoutDiagnostics(source);
		return assertNodeType(result.statements().first(), nodeType);
	}
}
