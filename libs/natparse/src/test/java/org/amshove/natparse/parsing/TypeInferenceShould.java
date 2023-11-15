package org.amshove.natparse.parsing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.natural.IAssignmentStatementNode;
import org.amshove.natparse.natural.IDataType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class TypeInferenceShould
{

	@ParameterizedTest
	@CsvSource(
		{
			"*OCC(#ARR),(I4)",
			"*ISN,(P10)",
			"*LENGTH(#STR),(I4)",
			"*TIMX,(T)",
			"*TIMESTMP,(B8)",
			"*TIMN,(N7)",
			"*DAT4J,(A7)",
			"*NET-USER,(A253)"
		}
	)
	void inferTheTypeOfSystemFunctions(String rhs, String expectedType)
	{
		assertInferredType(inferRhs(rhs), expectedType);
	}

	@ParameterizedTest
	@CsvSource(
		{
			"\"Hello\",(A5)",
			"5,(I1)"
		}
	)
	void inferTheTypeOfLiterals(String rhs, String expectedType)
	{
		assertInferredType(inferRhs(rhs), expectedType);
	}

	@Test
	void inferTheTypeOfAnArithmeticExpressionWithLiterals()
	{
		assertInferredType(inferRhs("10 + 10"), "(I1)");
	}

	@Test
	void inferTheTypeOfAnArithmeticExpressionWithMoreMath()
	{
		assertInferredType(inferRhs("10 + 10 * 5000 / 240001"), "(I4)");
	}

	@Test
	void inferTheTypeOfAnArithmeticExpressionWithSystemVarsMixedIn()
	{
		assertInferredType(inferRhs("*OCC(#ARR) + 1"), "(I4)");
	}

	// TODO: File based test with inferred type in comment
	// e.g.
	// #VAR := #NUM1 + *ISN /* TYPE: (P10)
	// #VAR := #BIGNUM + *OCC(#ARR) /* TYPE: (I4)
	// etc.

	private void assertInferredType(IDataType type, String expectedType)
	{
		assertThat(type.toShortString()).isEqualTo(expectedType);
	}

	private IDataType inferRhs(String rhs)
	{
		var source = "#TARGET := %s".formatted(rhs);
		var lexer = new Lexer();
		var tokens = lexer.lex(source, Path.of("SUB.NSN"));
		assertThat(tokens.diagnostics())
			.as("Expected the source to lex without diagnostics but got: %s".formatted(tokens.diagnostics().stream().map(IDiagnostic::message).collect(Collectors.joining(", "))))
			.isEmpty();
		var parseResult = new StatementListParser(new ModuleProviderStub()).parse(tokens);
		assertThat(parseResult.diagnostics())
			.as("Expected the source to parse without diagnostics but got: %s".formatted(parseResult.diagnostics().stream().map(IDiagnostic::message).collect(Collectors.joining(", "))))
			.isEmpty();

		var statement = (IAssignmentStatementNode) parseResult.result().statements().first();
		return TypeInference.inferType(statement.operand()).orElseThrow();
	}
}
