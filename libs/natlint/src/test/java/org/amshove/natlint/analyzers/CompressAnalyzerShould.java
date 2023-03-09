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
	@Test
	void raiseADiagnosticIfDefineWorkFileUsesACompressedPathWithoutLeavingNo()
	{
		testDiagnostics(
			"""
			DEFINE DATA
			LOCAL
			1 #PATH (A) DYNAMIC
			END-DEFINE
			COMPRESS 'Hello' 5 INTO #PATH
			DEFINE WORK FILE 1 #PATH
			END
			""",
			expectDiagnostic(4, CompressAnalyzer.COMPRESS_SHOULD_HAVE_LEAVING_NO)
		);
	}

	@Test
	void raiseADiagnosticIfDefineWorkFileUsesACompressedPathWithoutLeavingNoAndDefineWorkFileIsBeforeCompress()
	{
		testDiagnostics(
			"""
			DEFINE DATA
			LOCAL
			1 #PATH (A) DYNAMIC
			END-DEFINE
			DEFINE WORK FILE 1 #PATH
			COMPRESS 'Hello' 5 INTO #PATH
			END
			""",
			expectDiagnostic(5, CompressAnalyzer.COMPRESS_SHOULD_HAVE_LEAVING_NO)
		);
	}

	@Test
	void raiseADiagnosticIfDefineWorkFileUsesACompressedPathWithoutLeavingNoAndOneWithLeavingNo()
	{
		testDiagnostics(
			"""
			DEFINE DATA
			LOCAL
			1 #PATH (A) DYNAMIC
			END-DEFINE
			COMPRESS 'Hello' 5 INTO #PATH LEAVING NO
			COMPRESS 'Hello' 5 INTO #PATH
			DEFINE WORK FILE 1 #PATH
			END
			""",
			expectDiagnostic(5, CompressAnalyzer.COMPRESS_SHOULD_HAVE_LEAVING_NO)
		);
	}

	@Test
	void raiseNoDiagnosticIfCompressedPathIsCompressedWithLeavingNo()
	{
		testDiagnostics(
			"""
			DEFINE DATA
			LOCAL
			1 #PATH (A) DYNAMIC
			END-DEFINE
			DEFINE WORK FILE 1 #PATH
			COMPRESS 'Hello' 5 INTO #PATH LEAVING NO
			END
			""",
			expectNoDiagnosticOfType(CompressAnalyzer.COMPRESS_SHOULD_HAVE_LEAVING_NO)
		);
	}

	@Test
	void raiseNoDiagnosticIfThereIsACompressWithoutLeavingNoButItsTargetIsNotUsedForDefineWorkfile()
	{
		testDiagnostics(
			"""
			DEFINE DATA
			LOCAL
			1 #PATH (A) DYNAMIC
			1 #A (A) DYNAMIC
			END-DEFINE
			COMPRESS #PATH INTO #A
			DEFINE WORK FILE 1 #PATH
			END
			""",
			expectNoDiagnosticOfType(CompressAnalyzer.COMPRESS_SHOULD_HAVE_LEAVING_NO)
		);
	}

	@Test
	void raiseNoDiagnosticWhenTheCompressIsWithoutLeavingSpaceButOnlyHasOneOperand()
	{
		testDiagnostics(
			"""
			DEFINE DATA
			LOCAL
			1 #PATH (A) DYNAMIC
			1 #A (A) DYNAMIC
			END-DEFINE
			COMPRESS #A INTO #PATH
			DEFINE WORK FILE 1 #PATH
			END
			""",
			expectNoDiagnosticOfType(CompressAnalyzer.COMPRESS_SHOULD_HAVE_LEAVING_NO)
		);
	}
}
