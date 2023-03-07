package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CompressAnalyzerShould extends AbstractAnalyzerTest
{
	protected CompressAnalyzerShould()
	{
		super(new CompressAnalyzer());
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
			expectDiagnostic(6, CompressAnalyzer.COMPRESS_SHOULD_HAVE_NUMERIC)
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
			expectNoDiagnosticOfType(CompressAnalyzer.COMPRESS_SHOULD_HAVE_NUMERIC)
		);
	}

	@Test
	void raiseADiagnosticForCompressWithDelimitersWithoutAllWhenDelimiterIsSemicolon()
	{
		testDiagnostics(
			"""
			DEFINE DATA
			LOCAL
			1 #TEXT (A) DYNAMIC
			END-DEFINE

			COMPRESS 'Hello' 5 INTO #TEXT WITH DELIMITERS ';'

			END
			""",
			expectDiagnostic(5, CompressAnalyzer.COMPRESS_SHOULD_HAVE_ALL_DELIMITERS)
		);
	}

	@Test
	void raiseNoDiagnosticForCompressWithDelimitersWithAllWhenDelimiterIsSemicolon()
	{
		testDiagnostics(
			"""
			DEFINE DATA
			LOCAL
			1 #TEXT (A) DYNAMIC
			END-DEFINE

			COMPRESS 'Hello' 5 INTO #TEXT WITH ALL DELIMITERS ';'

			END
			""",
			expectNoDiagnosticOfType(CompressAnalyzer.COMPRESS_SHOULD_HAVE_ALL_DELIMITERS)
		);
	}

	@Test
	void raiseNoDiagnosticForCompressWithDelimitersWithAllWhenDelimiterIsAReference()
	{
		testDiagnostics(
			"""
			DEFINE DATA
			LOCAL
			1 #TEXT (A) DYNAMIC
			1 #DELIMITER (A1)
			END-DEFINE

			COMPRESS 'Hello' 5 INTO #TEXT WITH ALL DELIMITERS #DELIMITER

			END
			""",
			expectNoDiagnosticOfType(CompressAnalyzer.COMPRESS_SHOULD_HAVE_ALL_DELIMITERS)
		);
	}
}
