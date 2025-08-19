package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IParseJsonStatementNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ParseJsonStatementParsingShould extends StatementParseTest
{
	@Test
	void parseASimpleStatement()
	{
		var json = assertParsesSingleStatement("""
			PARSE JSON #JSON
				IGNORE
			END-PARSE
			""", IParseJsonStatementNode.class);

		assertIsVariableReference(json.jsonDocument(), "#JSON");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"ENCODED", "ENCODED IN"
	})
	void parseAJsonWithCodepage(String permutation)
	{
		var json = assertParsesSingleStatement("""
			PARSE JSON #JSON %s CODEPAGE #CODEPAGE
				IGNORE
			END-PARSE
			""".formatted(permutation), IParseJsonStatementNode.class);

		assertIsVariableReference(json.codePage(), "#CODEPAGE");
	}

	@Test
	void parseAJsonStatementWithAllOperands()
	{
		var json = assertParsesSingleStatement("""
			PARSE JSON #JSON ENCODED IN CODEPAGE #CODEPAGE
				INTO PATH #JSONPATH
				WITH SEPARATOR #PATHSEPARATOR
				NAME #JSONNAME
				VALUE #JSONVALUE
				GIVING #GIVING
				SUBCODE #SUBCODE

				IGNORE
			END-PARSE
			""", IParseJsonStatementNode.class);

		assertIsVariableReference(json.jsonPathSeparator(), "#PATHSEPARATOR");
		assertIsVariableReference(json.jsonAttributeName(), "#JSONNAME");
		assertIsVariableReference(json.jsonAttributeValue(), "#JSONVALUE");
		assertIsVariableReference(json.giving(), "#GIVING");
		assertIsVariableReference(json.subcode(), "#SUBCODE");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"INTO NAME #JSONNAME", "INTO VALUE #JSONVALUE", "INTO PATH #JSONPATH", "INTO PATH #JSONPATH NAME #JSONNAME", "INTO PATH #JSONPATH VALUE #JSONVALUE"
	})
	void parseAJsonWithDifferentPermutationsOfOperandOrder(String permutation)
	{
		assertParsesSingleStatement("""
			PARSE JSON #JSON %s
				IGNORE
			END-PARSE
			""".formatted(permutation), IParseJsonStatementNode.class);
	}
}
