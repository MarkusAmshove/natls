package org.amshove.natparse.parsing.statements;

import org.amshove.natparse.natural.IDefineWorkFileNode;
import org.amshove.natparse.natural.ILiteralNode;
import org.amshove.natparse.natural.IVariableReferenceNode;
import org.amshove.natparse.parsing.ParserError;
import org.amshove.natparse.parsing.StatementParseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class DefineWorkFileStatementParsingShould extends StatementParseTest
{
	@Test
	void parseASimpleWorkfile()
	{
		var define = assertParsesSingleStatement("""
			DEFINE WORK FILE 1
			""", IDefineWorkFileNode.class);

		assertThat(define.number().token().intValue()).isEqualTo(1);
	}

	@ParameterizedTest
	@ValueSource(ints =
	{
		-5, 33, 0, 55
	})
	void raiseADiagnosticIfTheWorkfileNumberIsInvalid(int value)
	{
		assertDiagnostic(
			"""
			DEFINE WORK FILE %d
			""".formatted(value),
			ParserError.INVALID_LITERAL_VALUE
		);
	}

	@Test
	void parseASimpleWorkfileWithPath()
	{
		var define = assertParsesSingleStatement("""
			DEFINE WORK FILE 1 '/path/to/file'
			""", IDefineWorkFileNode.class);

		assertThat(assertNodeType(define.path(), ILiteralNode.class).token().stringValue()).isEqualTo("/path/to/file");
	}

	@Test
	void parseASimpleWorkfileWithPathAsVariable()
	{
		var define = assertParsesSingleStatement("""
			DEFINE WORK FILE 1 #PATH
			""", IDefineWorkFileNode.class);

		assertIsVariableReference(define.path(), "#PATH");
	}

	@Test
	void raiseADiagnosticIfThePathHasAnInvalidType()
	{
		assertDiagnostic(
			"""
			DEFINE WORK FILE 1 5
			""",
			ParserError.TYPE_MISMATCH
		);
	}

	@Test
	void parseAWorkfileWithoutPathButWithType()
	{
		var work = assertParsesSingleStatement(
			"""
			DEFINE WORK FILE 1 TYPE 'CSV'
			""",
			IDefineWorkFileNode.class
		);

		assertThat(assertNodeType(work.type(), ILiteralNode.class).token().stringValue()).isEqualTo("CSV");
	}

	@Test
	void parseAWorkfileWithPathAndWithType()
	{
		var work = assertParsesSingleStatement(
			"""
			DEFINE WORK FILE 1 'file.txt' TYPE 'CSV'
			""",
			IDefineWorkFileNode.class
		);

		assertThat(assertNodeType(work.type(), ILiteralNode.class).token().stringValue()).isEqualTo("CSV");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"DEFAULT", "TRANSFER", "SAG", "ASCII", "ASCII-COMPRESSED", "ENTIRECONNECTION", "FORMATTED", "UNFORMATTED", "PORTABLE", "CSV"
	})
	void notRaiseADiagnosticForAllowedWorkfileTypes(String type)
	{
		assertParsesWithoutDiagnostics("""
			DEFINE WORK FILE 1 'file.txt' TYPE '%s'
			""".formatted(type));
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"JSON", "XML", "DONTKNOW"
	})
	void raiseADiagnosticForInvalidWorkfileTypes(String type)
	{
		assertDiagnostic(
			"""
			DEFINE WORK FILE 1 'file.txt' TYPE '%s'
			""".formatted(type),
			ParserError.INVALID_LITERAL_VALUE
		);
	}

	@Test
	void parseAWorkfileWithAttribute()
	{
		var work = assertParsesSingleStatement(
			"""
			DEFINE WORK FILE 1 'file.txt' ATTRIBUTES 'BOM'
			""",
			IDefineWorkFileNode.class
		);

		assertThat(assertNodeType(work.attributes(), ILiteralNode.class).token().stringValue()).isEqualTo("BOM");
	}

	@Test
	void parseAWorkfileWithAttributeButNoPath()
	{
		var work = assertParsesSingleStatement(
			"""
			DEFINE WORK FILE 1 ATTRIBUTES 'BOM'
			""",
			IDefineWorkFileNode.class
		);

		assertThat(assertNodeType(work.attributes(), ILiteralNode.class).token().stringValue()).isEqualTo("BOM");
	}

	@Test
	void parseAWorkfileWithMultipleAttributeCommaSeparated()
	{
		var work = assertParsesSingleStatement(
			"""
			DEFINE WORK FILE 1 'file.txt' ATTRIBUTES 'BOM,KEEP'
			""",
			IDefineWorkFileNode.class
		);

		assertThat(assertNodeType(work.attributes(), ILiteralNode.class).token().stringValue()).isEqualTo("BOM,KEEP");
	}

	@Test
	void parseAWorkfileWithMultipleAttributeWhitespaceSeparated()
	{
		var work = assertParsesSingleStatement(
			"""
			DEFINE WORK FILE 1 'file.txt' ATTRIBUTES 'BOM KEEP'
			""",
			IDefineWorkFileNode.class
		);

		assertThat(assertNodeType(work.attributes(), ILiteralNode.class).token().stringValue()).isEqualTo("BOM KEEP");
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"NOAPPEND", "APPEND", "DELETE", "KEEP", "BOM", "NOBOM", "KEEPCR", "REMOVECR"
	})
	void notRaiseADiagnosticForAllowedWorkfileAttributes(String attributes)
	{
		assertParsesWithoutDiagnostics("""
			DEFINE WORK FILE 1 'file.txt' ATTRIBUTES '%s'
			""".formatted(attributes));
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"WHAT", "IS", "THIS"
	})
	void raiseADiagnosticForWorkfilesWithInvalidAttributes(String attributes)
	{
		assertDiagnostic(
			"""
			DEFINE WORK FILE 1 'file.txt' ATTRIBUTES '%s'
			""".formatted(attributes),
			ParserError.INVALID_LITERAL_VALUE
		);
	}
}
