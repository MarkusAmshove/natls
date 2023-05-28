package org.amshove.natparse.parsing.statements;

import org.amshove.natparse.natural.*;
import org.amshove.natparse.parsing.ParserError;
import org.amshove.natparse.parsing.StatementParseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class CompressStatementParsingShould extends StatementParseTest
{
	@Test
	void parseASimpleCompress()
	{
		var compress = assertParsesSingleStatement("""
			COMPRESS 'Text' INTO #VAR
			""", ICompressStatementNode.class);

		assertThat(compress.isFull()).isFalse();
		assertThat(compress.isLeavingSpace()).isTrue();
		assertThat(compress.isWithDelimiters()).isFalse();

		assertThat(assertNodeType(compress.intoTarget(), IVariableReferenceNode.class).referencingToken().symbolName()).isEqualTo("#VAR");

		assertNodeOperand(compress, 0, ILiteralNode.class, "'Text'");
	}

	@Test
	void parseMultipleOperands()
	{
		var compress = assertParsesSingleStatement("""
			COMPRESS 'Text' #VAR2 #VAR3 INTO #VAR
			""", ICompressStatementNode.class);

		assertNodeOperand(compress, 0, ILiteralNode.class, "'Text'");
		assertNodeOperand(compress, 1, IVariableReferenceNode.class, "#VAR2");
		assertNodeOperand(compress, 2, IVariableReferenceNode.class, "#VAR3");
	}

	@Test
	void parseNumeric()
	{
		var compress = assertParsesSingleStatement("COMPRESS NUMERIC 1234 INTO #VAR", ICompressStatementNode.class);
		assertThat(compress.isNumeric()).isTrue();
		assertThat(compress.isFull()).isFalse();
	}

	@Test
	void parseFull()
	{
		var compress = assertParsesSingleStatement("COMPRESS FULL 1234 INTO #VAR", ICompressStatementNode.class);
		assertThat(compress.isNumeric()).isFalse();
		assertThat(compress.isFull()).isTrue();
	}

	@Test
	void parseNumericFull()
	{
		var compress = assertParsesSingleStatement("COMPRESS NUMERIC FULL 1234 INTO #VAR", ICompressStatementNode.class);
		assertThat(compress.isNumeric()).isTrue();
		assertThat(compress.isFull()).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"WITH DELIMITERS", "WITH ALL DELIMITERS", "WITH DELIMITER", "WITH ALL DELIMITER", "WITH"
	})
	void parseWithDelimiters(String permutation)
	{
		var compress = assertParsesSingleStatement("COMPRESS #VARS(*) INTO #VAR %s '*'".formatted(permutation), ICompressStatementNode.class);
		assertThat(compress.isWithDelimiters()).isTrue();
	}

	@Test
	void parseWithAllDelimiters()
	{
		var compress = assertParsesSingleStatement("COMPRESS #VARS(*) INTO #VAR WITH ALL DELIMITERS ';'", ICompressStatementNode.class);
		assertThat(compress.isWithDelimiters()).isTrue();
		assertThat(compress.isWithAllDelimiters()).isTrue();
		assertThat(assertNodeType(compress.delimiter(), ILiteralNode.class).token().stringValue()).isEqualTo(";");
	}

	@Test
	void parseDelimiterWithHexLiteral()
	{
		var compress = assertParsesSingleStatement("COMPRESS #VARS(*) INTO #VAR WITH DELIMITER H'0A'", ICompressStatementNode.class);
		assertThat(compress.isWithDelimiters()).isTrue();
		assertThat(assertNodeType(compress.delimiter(), ILiteralNode.class).token().source()).isEqualTo("H'0A'");
	}

	@Test
	void raiseADiagnosticIfTheDelimiterIsOfIncorrectType()
	{
		assertDiagnostic("COMPRESS #VAR INTO #VAR2 WITH ALL DELIMITERS 5", ParserError.TYPE_MISMATCH);
	}

	@Test
	void raiseADiagnosticIfTheDelimiterLengthIsGreaterThanOne()
	{
		assertDiagnostic("COMPRESS #VAR INTO #VAR2 WITH ALL DELIMITERS 'abc'", ParserError.INVALID_LENGTH_FOR_LITERAL);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"LEAVING NO", "LEAVING NO SPACE"
	})
	void parseLeavingNo(String permutation)
	{
		var compress = assertParsesSingleStatement("COMPRESS 1234 'Text' INTO #VAR %s".formatted(permutation), ICompressStatementNode.class);
		assertThat(compress.isLeavingSpace()).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"LEAVING SPACE", "LEAVING"
	})
	void parseLeavingSpace(String permutation)
	{
		var compress = assertParsesSingleStatement("COMPRESS 1234 'Text' INTO #VAR %s".formatted(permutation), ICompressStatementNode.class);
		assertThat(compress.isLeavingSpace()).isTrue();
	}

	@Test
	void parseCompressOverMultipleLines()
	{
		var compress = assertParsesSingleStatement("""
			COMPRESS #VAR
				#VAR2
				INTO
				#VAR3
			""", ICompressStatementNode.class);

		assertNodeOperand(compress, 0, IVariableReferenceNode.class, "#VAR");
		assertNodeOperand(compress, 1, IVariableReferenceNode.class, "#VAR2");
		assertThat(assertNodeType(compress.intoTarget(), IVariableReferenceNode.class).referencingToken().symbolName()).isEqualTo("#VAR3");
	}

	@Test
	void parseCompressWithSubstrings()
	{
		var compress = assertParsesSingleStatement("""
			COMPRESS SUBSTRING(#VAR, 1) #VAR2 INTO SUBSTRING(#VAR2, 2)
			""", ICompressStatementNode.class);

		assertNodeType(compress.operands().get(0), ISubstringOperandNode.class);
		assertNodeType(compress.intoTarget(), ISubstringOperandNode.class);
	}

	@Test
	void parseCompressTo()
	{
		var compress = assertParsesSingleStatement("""
			COMPRESS 'Text' TO #VAR
			""", ICompressStatementNode.class);

		assertNodeType(compress.operands().get(0), ILiteralNode.class);
		assertNodeType(compress.intoTarget(), IVariableReferenceNode.class);
	}

	@Test
	void parseCompressWithDelimiterWithoutStatingDelimiter()
	{
		var compress = assertParsesSingleStatement("""
			COMPRESS 'Text' TO #VAR WITH DELIMITER
			""", ICompressStatementNode.class);

		assertThat(compress.isWithDelimiters()).isTrue();
	}

	@Test
	void parseCompressWithDelimiterWithoutStatingDelimiterWhenStatementFollows()
	{
		ignoreModuleProvider();
		var statements = assertParsesWithoutDiagnostics("""
			COMPRESS 'Text' TO #VAR WITH DELIMITER
			CALLNAT 'MODULE'
			""");

		assertThat(statements.statements()).hasSize(2);
		assertNodeType(statements.statements().first(), ICompressStatementNode.class);
		assertNodeType(statements.statements().get(1), ICallnatNode.class);
	}

	@Test
	void parseCompressWithOperandParameterDF()
	{
		var compress = assertParsesSingleStatement("""
			COMPRESS *DATX(DF=L) TO #VAR
			""", ICompressStatementNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"LEAVING SPACE WITH DELIMITER ';'", "LEAVING WITH DELIMITER ';'",
		"LEAVING NO SPACE WITH ALL DELIMITERS ';'", "LEAVING WITH ALL DELIMITER ';'",
	})
	void raiseADiagnosticIfLeavingNoIsCombinedWithDelimiters(String permutation)
	{
		assertDiagnostic("""
			COMPRESS #TEXT TO #VAR %s
			""".formatted(permutation), ParserError.COMPRESS_HAS_LEAVING_NO_AND_DELIMITERS);
	}

	private void assertNodeOperand(ICompressStatementNode compress, int index, Class<? extends ITokenNode> operandType, String source)
	{
		assertThat(
			assertNodeType(compress.operands().get(index), operandType)
				.token().source()
		).isEqualTo(source);
	}
}
