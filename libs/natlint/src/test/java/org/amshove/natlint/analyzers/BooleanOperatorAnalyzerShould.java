package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class BooleanOperatorAnalyzerShould extends AbstractAnalyzerTest
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
	void reportNoDiagnosticForPreferredOperators(String operator)
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
			expectNoDiagnosticOfType(BooleanOperatorAnalyzer.DISCOURAGED_BOOLEAN_OPERATOR)
		);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"GT", "LT", "GE", "LE", "NE", "EQ"
	})
	void reportDiagnosticsForOperatorsThatAreNotPreferred(String operator)
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
			expectDiagnostic(4, BooleanOperatorAnalyzer.INVALID_NATUNIT_COMPARISON_OPERATOR)
		);
	}
}
