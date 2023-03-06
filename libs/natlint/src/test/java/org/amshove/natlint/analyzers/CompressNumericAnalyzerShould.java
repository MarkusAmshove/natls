package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.junit.jupiter.api.Test;

class CompressNumericAnalyzerShould extends AbstractAnalyzerTest
{
	protected CompressNumericAnalyzerShould()
	{
		super(new CompressNumericAnalyzer());
	}

	@Test
	void raiseADiagnosticIfAnOperandHasAFloatingNumberAndNoNumericIsPresent()
	{
		testDiagnostics("""
			DEFINE DATA
			LOCAL
			1 #VAR (N12,4)
			1 #TEXT (A) DYNAMIC
			END-DEFINE

			COMPRESS #VAR INTO #TEXT /* no NUMERIC present

			END
			""",
			expectDiagnostic(6, CompressNumericAnalyzer.COMPRESS_SHOULD_HAVE_NUMERIC)
		);
	}

	@Test
	void raiseNoDiagnosticIfAnOperandHasAFloatingNumberButNumericIsPresent()
	{
		testDiagnostics("""
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
