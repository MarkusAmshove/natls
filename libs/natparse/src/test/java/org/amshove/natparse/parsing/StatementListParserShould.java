package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.*;
import org.amshove.testhelpers.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@IntegrationTest
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
	void raiseADiagnosticWhenNoModuleIsPassed()
	{
		ignoreModuleProvider();
		assertDiagnostic("CALLNAT 1", ParserError.UNEXPECTED_TOKEN);
	}

	@Test
	void allowVariablesAsModuleReferences()
	{
		ignoreModuleProvider();
		var callnat = assertParsesSingleStatement("CALLNAT #THE-SUBPROGRAM", ICallnatNode.class);
		assertThat(callnat.referencingToken().kind()).isEqualTo(SyntaxKind.IDENTIFIER);
		assertThat(callnat.referencingToken().symbolName()).isEqualTo("#THE-SUBPROGRAM");
		assertThat(callnat.reference()).isNull();
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
	void allowTrailingSpacesInModuleNamesThatAreInStrings()
	{
		var calledSubprogram = new NaturalModule(null);
		moduleProvider.addModule("A-MODULE", calledSubprogram);

		var callnat = assertParsesSingleStatement("CALLNAT 'A-MODULE ' ", ICallnatNode.class);
		assertThat(callnat.reference()).isEqualTo(calledSubprogram);
		assertThat(calledSubprogram.callers()).contains(callnat);
	}

	@Test
	void findCalledSubprogramsWhenSourceContainsLowerCaseCharacters()
	{
		var calledSubprogram = new NaturalModule(null);
		moduleProvider.addModule("A-MODULE", calledSubprogram);

		var callnat = assertParsesSingleStatement("CALLNAT 'A-module'", ICallnatNode.class);
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
	void raiseADiagnosticWhenNoCopycodeIsPassed()
	{
		assertDiagnostic("INCLUDE 1", ParserError.UNEXPECTED_TOKEN);
	}

	@Test
	void parseASimpleFetch()
	{
		ignoreModuleProvider();
		var fetch = assertParsesSingleStatement("FETCH 'PROG'", IFetchNode.class);
		assertThat(fetch.referencingToken().kind()).isEqualTo(SyntaxKind.STRING);
		assertThat(fetch.referencingToken().stringValue()).isEqualTo("PROG");
	}

	@Test
	void parseASimpleFetchReturn()
	{
		ignoreModuleProvider();
		var fetch = assertParsesSingleStatement("FETCH RETURN 'PROG'", IFetchNode.class);
		assertThat(fetch.referencingToken().kind()).isEqualTo(SyntaxKind.STRING);
		assertThat(fetch.referencingToken().stringValue()).isEqualTo("PROG");
	}

	@Test
	void parseASimpleFetchRepeat()
	{
		ignoreModuleProvider();
		var fetch = assertParsesSingleStatement("FETCH REPEAT 'PROG'", IFetchNode.class);
		assertThat(fetch.referencingToken().kind()).isEqualTo(SyntaxKind.STRING);
		assertThat(fetch.referencingToken().stringValue()).isEqualTo("PROG");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"",
		"REPEAT",
		"RETURN"
	})
	void parseAFetchWithVariables(String fetchType)
	{
		ignoreModuleProvider();
		var fetch = assertParsesSingleStatement("FETCH %s #MYVAR".formatted(fetchType), IFetchNode.class);
		assertThat(fetch.referencingToken().kind()).isEqualTo(SyntaxKind.IDENTIFIER);
		assertThat(fetch.referencingToken().symbolName()).isEqualTo("#MYVAR");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"",
		"REPEAT",
		"RETURN"
	})
	void parseAFetchWithQualifiedVariables(String fetchType)
	{
		ignoreModuleProvider();
		var fetch = assertParsesSingleStatement("FETCH %s #MYGROUP.#MYVAR".formatted(fetchType), IFetchNode.class);
		assertThat(fetch.referencingToken().kind()).isEqualTo(SyntaxKind.IDENTIFIER);
		assertThat(fetch.referencingToken().symbolName()).isEqualTo("#MYGROUP.#MYVAR");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"",
		"RETURN",
		"REPEAT"
	})
	void resolveExternalModulesForAFetchStatement(String fetchSource)
	{
		var program = new NaturalModule(null);
		moduleProvider.addModule("PROG", program);

		var fetch = assertParsesSingleStatement("FETCH %s 'PROG'".formatted(fetchSource), IFetchNode.class);
		assertThat(fetch.reference()).isEqualTo(program);
	}

	@Test
	void parseAnEndNode()
	{
		var endNode = assertParsesSingleStatement("END", IEndNode.class);
		assertThat(endNode.descendants()).isNotEmpty();
	}

	private <T extends IStatementNode> T assertParsesSingleStatement(String source, Class<T> nodeType)
	{
		var result = super.assertParsesWithoutDiagnostics(source);
		return assertNodeType(result.statements().first(), nodeType);
	}
}
