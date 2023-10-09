package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.junit.jupiter.api.Test;

class ValueTruncationAnalyzerShould extends AbstractAnalyzerTest
{
	@Test
	void raiseADiagnosticWhenAConstInitializerIsTruncatedForCompatibleFormats()
	{
		testDiagnostics("""
			DEFINE DATA LOCAL
			1 #C-CONST (A1) CONST<20>
			END-DEFINE
			""", expectDiagnostic(1, ValueTruncationAnalyzer.VALUE_TRUNCATED));
	}

	@Test
	void raiseADiagnosticWhenAnInitInitializerIsTruncatedForCompatibleFormats()
	{
		testDiagnostics("""
			DEFINE DATA LOCAL
			1 #C-CONST (A1) INIT<20>
			END-DEFINE
			""", expectDiagnostic(1, ValueTruncationAnalyzer.VALUE_TRUNCATED));
	}

	@Test
	void reportADiagnosticWhenNumericAssignmentValuesGetTruncated()
	{
		testDiagnostics(
			"""
				DEFINE DATA LOCAL
				1 #CONST-N1-I4 (I4) CONST<1>
				1 #N1 (N1)
				1 #I1 (I1)
				END-DEFINE
				#N1 := 23
				#I1 := 128
				END
				""",
			expectDiagnostic(5, ValueTruncationAnalyzer.VALUE_TRUNCATED),
			expectDiagnostic(6, ValueTruncationAnalyzer.VALUE_TRUNCATED)
		);
	}

	@Test
	void reportADiagnosticWhenNumericAssignmentValuesGetTruncatedWithCompatibleTargetFormat()
	{
		testDiagnostics("""
			DEFINE DATA LOCAL
			1 #CONST-N1-I4 (I4) CONST<1>
			1 #A1 (A1)
			END-DEFINE
			#A1 := 10
			END
			""", expectDiagnostic(4, ValueTruncationAnalyzer.VALUE_TRUNCATED));
	}

	@Test
	void reportADiagnosticWhenAlphanumericAssignmentValuesGetTruncated()
	{
		testDiagnostics("""
			DEFINE DATA LOCAL
			1 #A1 (A1)
			END-DEFINE
			#A1 := 'AB'
			END
			""", expectDiagnostic(3, ValueTruncationAnalyzer.VALUE_TRUNCATED));
	}

	protected ValueTruncationAnalyzerShould()
	{
		super(new ValueTruncationAnalyzer());
	}
}
