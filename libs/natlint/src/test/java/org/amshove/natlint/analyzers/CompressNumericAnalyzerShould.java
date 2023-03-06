package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CompressNumericAnalyzerShould extends AbstractAnalyzerTest
{
	protected CompressNumericAnalyzerShould()
	{
		super(new CompressNumericAnalyzer());
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"N12,4", "F8"
	})
	void raiseADiagnosticIfAnOperandHasAFloatingNumberAndNoNumericIsPresent(String type)
	{
		testDiagnostics(
			"""
			DEFINE DATA
			LOCAL
			1 #VAR (%s)
			1 #TEXT (A) DYNAMIC
			END-DEFINE

			COMPRESS #VAR INTO #TEXT /* no NUMERIC present

			END
			""".formatted(type),
			expectDiagnostic(6, CompressNumericAnalyzer.COMPRESS_SHOULD_HAVE_NUMERIC)
		);
	}

	@Test
	void raiseNoDiagnosticIfAnOperandHasAFloatingNumberButNumericIsPresent()
	{
		testDiagnostics(
			"""
			DEFINE DATA
			LOCAL
			1 #VAR (N12,4)
			1 #TEXT (A) DYNAMIC
			END-DEFINE

			COMPRESS NUMERIC #VAR INTO #TEXT /* NUMERIC present

			END
			""",
			expectNoDiagnosticOfType(CompressNumericAnalyzer.COMPRESS_SHOULD_HAVE_NUMERIC)
		);
	}
}
