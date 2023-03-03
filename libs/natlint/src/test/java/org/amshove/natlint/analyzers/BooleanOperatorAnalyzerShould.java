package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BooleanOperatorAnalyzerShould extends AbstractAnalyzerTest
{
	protected BooleanOperatorAnalyzerShould()
	{
		super(new BooleanOperatorAnalyzer());
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		">", "<", ">=", "<=", "<>", "="
	})
	void reportNoDiagnosticForPreferredSignOperators(String operator)
	{
		configureEditorConfig("""
			[*]
			natls.operators=sign
			""");
		testDiagnostics(
			"""
			DEFINE DATA LOCAL
			END-DEFINE

			IF 5 %s 2
			  IGNORE
			END-IF
			END
			""".formatted(operator),
			expectNoDiagnosticOfType(BooleanOperatorAnalyzer.DISCOURAGED_BOOLEAN_OPERATOR)
		);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		">", "<", ">=", "<=", "<>", "="
	})
	void reportDiagnosticsForOperatorSignsIfShortFormIsPreferred(String operator)
	{
		configureEditorConfig("""
			[*]
			natls.operators=short
			""");
		testDiagnostics(
			"""
			DEFINE DATA LOCAL
			END-DEFINE

			IF 5 %s 2
			  IGNORE
			END-IF
			END
			""".formatted(operator),
			expectDiagnostic(3, BooleanOperatorAnalyzer.DISCOURAGED_BOOLEAN_OPERATOR)
		);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"GT", "LT", "GE", "LE", "NE", "EQ"
	})
	void reportNoDiagnosticForShortFormOperatorsIfPreferred(String operator)
	{
		configureEditorConfig("""
			[*]
			natls.operators=short
			""");
		testDiagnostics(
			"""
			DEFINE DATA LOCAL
			END-DEFINE

			IF 5 %s 2
			  IGNORE
			END-IF
			END
			""".formatted(operator),
			expectNoDiagnosticOfType(BooleanOperatorAnalyzer.DISCOURAGED_BOOLEAN_OPERATOR)
		);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"GT", "LT", "GE", "LE", "NE", "EQ"
	})
	void reportDiagnosticsForShortFormOperatorsThatAreNotPreferred(String operator)
	{
		testDiagnostics(
			"""
			DEFINE DATA LOCAL
			END-DEFINE

			IF 5 %s 2
			  IGNORE
			END-IF
			END
			""".formatted(operator),
			expectDiagnostic(3, BooleanOperatorAnalyzer.DISCOURAGED_BOOLEAN_OPERATOR)
		);
	}

	@Test
	void notEnforceEqualsOverEqForNatUnitTests(@ProjectName("natunit") NaturalProject project)
	{
		testDiagnostics(
			project.findModule("TCEQTEST"),
			expectNoDiagnosticOfType(BooleanOperatorAnalyzer.DISCOURAGED_BOOLEAN_OPERATOR)
		);
	}

	@Test
	void enforceEqOverEqualsForNatUnitTests(@ProjectName("natunit") NaturalProject project)
	{
		testDiagnostics(
			project.findModule("TCTEST"),
			expectDiagnostic(8, BooleanOperatorAnalyzer.INVALID_NATUNIT_COMPARISON_OPERATOR)
		);
	}

	@Test
	void reportNoDiagnosticForEmptyFunctionCalls(@ProjectName("natunit") NaturalProject project)
	{
		testDiagnostics(
			project.findModule("CALLFUNC"),
			expectNoDiagnosticOfType(BooleanOperatorAnalyzer.DISCOURAGED_BOOLEAN_OPERATOR)
		);
	}
}
