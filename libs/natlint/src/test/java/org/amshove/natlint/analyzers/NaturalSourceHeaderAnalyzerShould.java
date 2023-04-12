package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.junit.jupiter.api.Test;

class NaturalSourceHeaderAnalyzerShould extends AbstractAnalyzerTest
{
	protected NaturalSourceHeaderAnalyzerShould()
	{
		super(new NaturalSourceHeaderAnalyzer());
	}

	@Test
	void reportNoDiagnosticIfProgrammingModeIsSpecified()
	{
		testDiagnostics(
			"""
			* >Natural Source Header 000000
			* :Mode S
			* :CP
			* <Natural Source Header
			DEFINE DATA LOCAL
			END-DEFINE

			END
			""",
			expectNoDiagnosticOfType(NaturalSourceHeaderAnalyzer.MISSING_SOURCE_HEADER)
		);
	}

	@Test
	void reportADiagnosticIfTheSourceHeaderIsMissing()
	{
		testDiagnostics(
			"""
			DEFINE DATA LOCAL
			END-DEFINE

			END
			""",
			expectDiagnostic(0, NaturalSourceHeaderAnalyzer.MISSING_SOURCE_HEADER)
		);
	}
}
