package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.junit.jupiter.api.Test;

class ConditionAlwaysFalseAnalyzerShould extends AbstractAnalyzerTest
{
	protected ConditionAlwaysFalseAnalyzerShould()
	{
		super(new ConditionAlwaysFalseAnalyzer());
	}

	@Test
	void reportADecideConditionThatCanNeverBeMet()
	{
		testDiagnostics(
			"""
			DEFINE DATA LOCAL
			1 #AVAR (A1)
			END-DEFINE
			DECIDE ON FIRST VALUE OF #AVAR
			VALUE 'Hello'
			IGNORE
			NONE
			IGNORE
			END-DECIDE
			END
			""",
			expectDiagnostic(4, ConditionAlwaysFalseAnalyzer.CONDITION_ALWAYS_FALSE)
		);
	}

	@Test
	void notReportADecideConditionWhichFitsTrimmed()
	{
		testDiagnostics(
			"""
			DEFINE DATA LOCAL
			1 #AVAR (A1)
			END-DEFINE
			DECIDE ON FIRST VALUE OF #AVAR
			VALUE 'H    '
			IGNORE
			NONE
			IGNORE
			END-DECIDE
			END
			""",
			expectNoDiagnostic(4, ConditionAlwaysFalseAnalyzer.CONDITION_ALWAYS_FALSE)
		);
	}
}
