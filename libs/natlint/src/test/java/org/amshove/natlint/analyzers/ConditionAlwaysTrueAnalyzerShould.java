package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.junit.jupiter.api.Test;

class ConditionAlwaysTrueAnalyzerShould extends AbstractAnalyzerTest
{
	protected ConditionAlwaysTrueAnalyzerShould()
	{
		super(new ConditionAlwaysTrueAnalyzer());
	}

	@Test
	void reportADiagnosticIfALocalVariableIsCheckedWithSpecified()
	{
		testDiagnostics(
			"""
			DEFINE DATA LOCAL
			1 #AVAR (A1)
			END-DEFINE
			IF #AVAR SPECIFIED
			IGNORE
			END-IF
			END
			""",
			expectDiagnostic(3, ConditionAlwaysTrueAnalyzer.CONDITION_ALWAYS_TRUE)
		);
	}

	@Test
	void reportADiagnosticIfANonOptionalParameterVariableIsCheckedWithSpecified()
	{
		testDiagnostics(
			"""
				DEFINE DATA PARAMETER
				1 #AVAR (A1)
				END-DEFINE
				IF #AVAR SPECIFIED
				IGNORE
				END-IF
				END
				""",
			expectDiagnostic(3, ConditionAlwaysTrueAnalyzer.CONDITION_ALWAYS_TRUE)
		);
	}

	@Test
	void reportNoDiagnosticIfAnOptionalParameterVariableIsCheckedWithSpecified()
	{
		testDiagnostics(
			"""
				DEFINE DATA PARAMETER
				1 #AVAR (A1) OPTIONAL
				END-DEFINE
				IF #AVAR SPECIFIED
				IGNORE
				END-IF
				END
				""",
			expectNoDiagnosticOfType(ConditionAlwaysTrueAnalyzer.CONDITION_ALWAYS_TRUE)
		);
	}
}
