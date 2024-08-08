package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.amshove.natparse.parsing.ParserError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UnreachableCodeAnalyzerShould extends AbstractAnalyzerTest
{

	protected UnreachableCodeAnalyzerShould()
	{
		super(new UnreachableCodeAnalyzer());
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"TOP", "BOTTOM", "ROUTINE", "MODULE"
	})
	void raiseADiagnosticWhenCodeIsUnreachableAfterEscape(String direction)
	{
		testDiagnostics(
			"""
			DEFINE DATA LOCAL
			END-DEFINE
			ESCAPE %s
			WRITE 'Hi'
			END
			""".formatted(direction),
			expectDiagnostic(3, UnreachableCodeAnalyzer.UNREACHABLE_CODE)
		);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"TOP", "BOTTOM", "ROUTINE", "MODULE"
	})
	void notRaiseADiagnosticWhenEscapeIsTheLastStatementInABlock(String direction)
	{
		testDiagnostics(
			"""
			DEFINE DATA LOCAL
			END-DEFINE
			IF TRUE
			ESCAPE %s
			END-IF
			END
			""".formatted(direction),
			expectNoDiagnosticOfType(UnreachableCodeAnalyzer.UNREACHABLE_CODE)
		);
	}

	@Test
	void notRaiseADiagnosticOnEndNodes()
	{
		testDiagnostics(
			"""
			DEFINE DATA LOCAL
			END-DEFINE
			ESCAPE MODULE
			END
			""",
			expectNoDiagnosticOfType(UnreachableCodeAnalyzer.UNREACHABLE_CODE)
		);
	}

	@Test
	void notRaiseADiagnosticOnSubroutineDefinitions()
	{
		testDiagnostics(
			"""
			DEFINE DATA LOCAL
			END-DEFINE
			ESCAPE MODULE
			DEFINE SUBROUTINE MY-SUB
			IGNORE
			END-SUBROUTINE
			END
			""",
			expectNoDiagnosticOfType(UnreachableCodeAnalyzer.UNREACHABLE_CODE)
		);
	}

	@Test
	void notRaiseADiagnosticOnOnErrorDefinitions()
	{
		testDiagnostics(
			"""
			DEFINE DATA LOCAL
			END-DEFINE
			ESCAPE MODULE
			ON ERROR
			IGNORE
			END-ERROR
			END
			""",
			expectNoDiagnosticOfType(UnreachableCodeAnalyzer.UNREACHABLE_CODE)
		);
	}

	@Test
	void raiseADiagnosticWhenCodeIsUnreachableAfterTerminate()
	{
		testDiagnostics(
			"""
			DEFINE DATA LOCAL
			END-DEFINE
			TERMINATE 1
			WRITE 'Hi'
			END
			""",
			expectDiagnostic(3, UnreachableCodeAnalyzer.UNREACHABLE_CODE)
		);
	}

	@Test
	void raiseADiagnosticWhenCodeIsUnreachableAfterFetch()
	{
		allowParserError(ParserError.UNRESOLVED_MODULE.id());
		testDiagnostics(
			"""
			DEFINE DATA LOCAL
			END-DEFINE
			FETCH 'MODULE'
			WRITE 'Hi'
			END
			""",
			expectDiagnostic(3, UnreachableCodeAnalyzer.UNREACHABLE_CODE)
		);
	}

	@Test
	void notRaiseADiagnosticForStatementsAfterFetchReturn()
	{
		allowParserError(ParserError.UNRESOLVED_MODULE.id());
		testDiagnostics(
			"""
			DEFINE DATA LOCAL
			END-DEFINE
			FETCH RETURN 'MODULE'
			WRITE 'Hi'
			END
			""",
			expectNoDiagnosticOfType(UnreachableCodeAnalyzer.UNREACHABLE_CODE)
		);
	}

}
