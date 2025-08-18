package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IParseXmlStatementNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class ParseXmlStatementParsingShould extends StatementParseTest
{
	@Test
	void parseASimpleStatement()
	{
		var statement = assertParsesSingleStatement("""
			PARSE XML #XML
			    IGNORE
			END-PARSE
			""", IParseXmlStatementNode.class);

		assertIsVariableReference(statement.xmlDocument(), "#XML");
		assertThat(statement.body().statements()).hasSize(1);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"", "INTO"
	})
	void parseAStatementWithOptionalIntoOperands(String permutation)
	{
		var statement = assertParsesSingleStatement("""
			PARSE XML #XML %s
				PATH #XMLPATH NAME #XMLNAME VALUE #XMLVALUE
			    IGNORE
			END-PARSE
			""".formatted(permutation), IParseXmlStatementNode.class);

		assertIsVariableReference(statement.xmlElementPath(), "#XMLPATH");
		assertIsVariableReference(statement.xmlElementName(), "#XMLNAME");
		assertIsVariableReference(statement.xmlElementValue(), "#XMLVALUE");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"", "NORMALIZE"
	})
	void parseAStatementWithNamespaceAndPrefix(String permutation)
	{
		var statement = assertParsesSingleStatement("""
			PARSE XML #XML %s NAMESPACE #XMLNAMESPACE PREFIX #XMLPREFIX
			    IGNORE
			END-PARSE
			""".formatted(permutation), IParseXmlStatementNode.class);

		assertIsVariableReference(statement.xmlNamespace(), "#XMLNAMESPACE");
		assertIsVariableReference(statement.xmlPrefix(), "#XMLPREFIX");
	}
}
